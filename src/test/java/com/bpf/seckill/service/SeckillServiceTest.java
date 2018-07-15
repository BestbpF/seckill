package com.bpf.seckill.service;

import com.bpf.seckill.dto.Exposer;
import com.bpf.seckill.dto.SeckillExcution;
import com.bpf.seckill.entity.Seckill;
import com.bpf.seckill.exception.RepeatKillException;
import com.bpf.seckill.exception.SeckillCloseException;
import com.bpf.seckill.exception.SeckillException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private  final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> seckills = seckillService.getSeckillList();
        logger.info("list = {}", seckills);
    }

    @Test
    public void getById() {
        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill = {}", seckill);
    }


    /**
     * 秒杀核心逻辑完整测试，注意重复秒杀测试
     */
    @Test
    public void testSeckillLogic() {
        long id = 1002;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()){
            logger.info("exposer = {}", exposer);
            long phone = 18234864271l;
            String md5 = exposer.getMd5();
            try {
                SeckillExcution seckillExcution = seckillService.excuteSeckill(id, phone, md5);
                logger.info("result = {}", seckillExcution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage());
            } catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }
        } else {
            logger.warn("exposer = {}", exposer);
        }
    }

    @Test
    public void testSeckillLogicByProcedure() {
        long id = 1002;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()){
            logger.info("exposer = {}", exposer);
            long phone = 18234864271l;
            String md5 = exposer.getMd5();
            SeckillExcution seckillExcution = seckillService.excuteSeckillByProcedure(id, phone, md5);
            logger.info("result = {}", seckillExcution);
        } else {
            logger.warn("exposer = {}", exposer);
        }
    }

}