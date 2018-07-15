package com.bpf.seckill.dao;

import com.bpf.seckill.entity.SuccessKilled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {
        int insertCount = successKilledDao.insertSuccessKilled(1000,13994835045l);
        System.out.println(insertCount);
    }

    @Test
    public void queryBySeckillId() {
        SuccessKilled successKilled = successKilledDao.queryBySeckillId(1000, 13994835045l);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());
    }
}