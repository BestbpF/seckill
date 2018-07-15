package com.bpf.seckill.service;

import com.bpf.seckill.dto.Exposer;
import com.bpf.seckill.dto.SeckillExcution;
import com.bpf.seckill.entity.Seckill;
import com.bpf.seckill.exception.RepeatKillException;
import com.bpf.seckill.exception.SeckillCloseException;
import com.bpf.seckill.exception.SeckillException;
import org.springframework.stereotype.Service;

import java.util.List;

/*
* 站在使用者的角度去设计业务接口
* 1、方法定义粒度
* 2、参数
* 3、返回类型
* */
public interface SeckillService {

    List<Seckill> getSeckillList();

    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，这里抛出异常是为了支持spring声明是事务
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExcution excuteSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException;

    SeckillExcution excuteSeckillByProcedure(long seckillId, long userPhone, String md5);
}
