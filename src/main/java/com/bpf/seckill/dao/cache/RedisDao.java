package com.bpf.seckill.dao.cache;

import com.bpf.seckill.entity.Seckill;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JedisPool jedisPool;
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }


    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        Jedis jedis = null;
        String key = "seckill:" + seckillId;
        try {
            jedis = jedisPool.getResource();
            //并没有实现内部序列化操作
            //采用自定义序列化protostuff
            //空间性能远远强于java序列化
            byte[] bytes = jedis.get(key.getBytes());
            if(bytes != null){
                Seckill seckill = schema.newMessage();
                ProtobufIOUtil.mergeFrom(bytes, seckill, schema);
                logger.debug("get seckill : " + seckill + "from redis");
                return seckill;
            }

        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckill.getSeckillId();
            byte[] bytes = ProtobufIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            //超时缓存
            int timeout = 60 * 60;//1小时
            String result = jedis.setex(key.getBytes(), timeout, bytes);
            logger.debug("put seckill : " + seckill + "into redis");
            return result;
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return null;
    }
}
