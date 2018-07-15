package com.bpf.seckill.service.impl;

import com.bpf.seckill.dao.SeckillDao;
import com.bpf.seckill.dao.SuccessKilledDao;
import com.bpf.seckill.dao.cache.RedisDao;
import com.bpf.seckill.dto.Exposer;
import com.bpf.seckill.dto.SeckillExcution;
import com.bpf.seckill.entity.Seckill;
import com.bpf.seckill.entity.SuccessKilled;
import com.bpf.seckill.enums.SeckillStatEnum;
import com.bpf.seckill.exception.RepeatKillException;
import com.bpf.seckill.exception.SeckillCloseException;
import com.bpf.seckill.exception.SeckillException;
import com.bpf.seckill.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;
    //md5盐值字符串，用于混淆md5
    private final String slat = "jasdhiufiahfoiahfioio2ih2o@foihao@!o92";
    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：redis缓存优化
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null){
                return new Exposer(false,seckillId);
            }
            redisDao.putSeckill(seckill);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date now = new Date();

        if(now.getTime() < startTime.getTime() || now.getTime() > endTime.getTime()){
            return new Exposer(false, seckillId, now.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转换特定字符串的过程
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    @Override
    @Transactional
   /*
   * 1、spring控制事务会与数据库之间产生网络延迟以及可能的GC等，造成瓶颈（mysql其实本身效率很高）
   * 2、行级锁正在commit(rollback)后释放，需要减少行级锁持有时间(调整源码顺序---先插入后更新)
   * 3、客户端需要确认update影响记录数然后才执行插入，可以把这个逻辑放在mysql服务器端（存储过程）
   * */

    /*
    * 使用注解控制事务的优点
    * 1、开发团队达成一致约定，明确标注事务方法的编程风格
    * 2、保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，RPC/HTTP等
    * 3、不是所有的方法都需要事务，比如说只有一条修改操作，只读操作等
    * */
    public SeckillExcution excuteSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite!!!!");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        //1、将减库存与记录购买行为操作互换位置，因为update操作才会拿到行级锁,但是行级锁在commit(rollback)后释放
        // 2、所以调换位置会减少一半的持有锁时间
        Date now = new Date();
        try {

            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //seckillId与userPhone组成联合主键，唯一
            if(insertCount <= 0){
                throw  new RepeatKillException("seckill repeated!!!!");
            } else {
                //减库存，热点商品竞争（此处拿到行级锁）
                int updateCount = seckillDao.reduceNumber(seckillId, now);
                if(updateCount <= 0){
                    //没有更新到记录，秒杀结束
                    throw  new SeckillCloseException("seckill is closed!!!!");
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryBySeckillId(seckillId, userPhone);
                    return new SeckillExcution(seckillId,SeckillStatEnum.SUCCESS, successKilled);
                }

            }
        } catch (SeckillCloseException e1){
            throw e1;
        } catch (RepeatKillException e2){
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译器异常转化为运行期异常
            throw new SeckillException("seckill inner error : " + e.getMessage());
        }
    }

    /**
     * 使用存储过程减少客户端到mysql的网络延迟以及GC
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    @Override
    public SeckillExcution excuteSeckillByProcedure(long seckillId, long userPhone, String md5) {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExcution(seckillId, SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> params = new HashMap<>();
        params.put("seckillId", seckillId);
        params.put("phone", userPhone);
        params.put("killTime", killTime);
        params.put("result", null);
        //存储过程结束后result被赋值

        try {
            seckillDao.killByProcedure(params);
            //获取result
            int result = MapUtils.getInteger(params, "result", -2);
            if(result == 1){
                SuccessKilled sk = successKilledDao.queryBySeckillId(seckillId, userPhone);
                return new SeckillExcution(seckillId, SeckillStatEnum.SUCCESS, sk);
            }else {
                return new SeckillExcution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExcution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
    }

    private String getMD5(long seckillId){
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
