package com.bpf.seckill.dao;

import com.bpf.seckill.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledDao {

    /**
     * 插入购买明细，可过滤重复(通过使用联合主键以及select ingore来实现)
     * @param seckillId
     * @param userPhone
     * @return
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据id查询SuccessKilled并携带秒杀产品对象实体
     * @param seckillId
     * @return
     *
     */
    SuccessKilled queryBySeckillId(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
