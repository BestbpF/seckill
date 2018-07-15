package com.bpf.seckill.dao;

import com.bpf.seckill.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SeckillDao {


    /**
     * 减库存
     * @param seckillId
     * @param killTime
     * @return
     */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    /**
     * 根据id查询库存
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     *分页查询
     * @param offset
     * @param limit
     * @return
     */
    /*
    * 当有两个参数时，mybatis无法自动识别，需要使用@Param注解
    * */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 使用存储过程执行秒杀
     * @param param
     */
    void killByProcedure(Map<String, Object> param);
}
