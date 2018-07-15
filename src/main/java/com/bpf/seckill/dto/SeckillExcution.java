package com.bpf.seckill.dto;

import com.bpf.seckill.entity.SuccessKilled;
import com.bpf.seckill.enums.SeckillStatEnum;

/**
 * 封装秒杀执行后的结果
 */
public class SeckillExcution {
    private long seckiLLid;

    //秒杀结果状态
    private int state;
    //状态表示
    private String stateInfo;
    //秒杀成功对象
    private SuccessKilled successKilled;

    public SeckillExcution(long seckiLLid, SeckillStatEnum statEnum, SuccessKilled successKilled) {
        this.seckiLLid = seckiLLid;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
        this.successKilled = successKilled;
    }

    public SeckillExcution(long seckiLLid, SeckillStatEnum statEnum) {
        this.seckiLLid = seckiLLid;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
    }

    public long getSeckiLLid() {
        return seckiLLid;
    }

    public void setSeckiLLid(long seckiLLid) {
        this.seckiLLid = seckiLLid;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExcution{" +
                "seckiLLid=" + seckiLLid +
                ", state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", successKilled=" + successKilled +
                '}';
    }
}
