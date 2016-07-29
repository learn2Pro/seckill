package org.seckill.dao.cache;

import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
	private final JedisPool jedisPool;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);
	public RedisDao(String ip, int port){
		jedisPool = new JedisPool(ip,port);
	}
	
	/**
	 * 获取Seckill缓存
	 * @param seckillId
	 * @return
	 */
	public Seckill getSeckill(long seckillId){
        //redis操作逻辑

        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                //并没有实现内部序列化操作
                // get->byte[] ->反序列化 ->Object(Seckill)
                //采用自定义序列化
                //protostuff : pojo
                byte[] bytes = jedis.get(key.getBytes());
                //缓存重新获取
                if (null != bytes) {
                    //空对象
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    //seckill被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
	
	/**
	 * 缓存Seckill对象
	 * @param seckill
	 * @return
	 */
	public String putSeckill(Seckill seckill){
        //set Object[Seckill] -> 序列化 -> byte[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                //timeout cache, here set one hour
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return null;
    }
	
	/**
	 * 获取token
	 * @param seckill
	 * @return
	 */
	public String getToken(long seckillId){
        //set Object[Seckill] -> 序列化 -> byte[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "token:" + seckillId;
                return jedis.rpop(key);
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return null;
    }

	public void setTokenSet(String md5, long seckillId) {
		try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "tokenSet:" + seckillId;
                jedis.sadd(key,md5); 
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
	}

	public boolean verifyToken(String md5, long seckillId) {
		
		try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "tokenSet:" + seckillId;
                return jedis.sismember(key, md5); 
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
		return false;
	}
}
