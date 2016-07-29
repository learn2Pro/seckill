package org.seckill.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.seckill.constant.CacheConstant;
import org.seckill.util.TKillCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: tang
 * Date: 16-7-29
 * Time: 上午10:05
 * To change this template use File | Settings | File Templates.
 */
public class KillCacheTemplate {


    private static TKillCacheUtils tKillCacheUtils;


    private static final Logger logger = LoggerFactory.getLogger(KillCacheTemplate.class);

    /**
     * 缓存读取操作。首先从缓存中获取，没有则中数据库中查询，查询完成后再设置缓存
     *
     * @param key               缓存key
     * @param expiration        缓存持久时间,设置为null则表示永久
     * @param clazz             缓存对象类型
     * @param killCacheCallback 数据库操作
     * @param <T>
     * @return 返回制定类型的对象
     */
    public static <T> T get(String key, Integer expiration, Class<T> clazz, KillCacheCallback killCacheCallback) {
        Object object = tKillCacheUtils.get(key);
        if (object == null) {
            try {
                object = killCacheCallback.doGet();
            } catch (Exception e) {
                logger.error("Failed to get object by cache template!", e);
                return null;
            }
            if (object != null) {
                if (object instanceof Collection) {
                    if (CollectionUtils.isNotEmpty((Collection) object)) {
                        set(key, expiration, object);
                    }
                } else if (object instanceof String) {
                    if (StringUtils.isNotBlank((String) object)) {
                        set(key, expiration, object);
                    }
                } else {
                    set(key, expiration, object);
                }

            }
        }
        return (T) object;
    }

    /**
     * 根据缓存key获取缓存对象
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T get(String key, Class<T> clazz) {
        return (T) tKillCacheUtils.get(key);
    }

    /**
     * 设置对象到缓存中
     *
     * @param key        缓存key
     * @param expiration 缓存持久时间,设置为null则表示永久
     * @param object     缓存对象
     */
    public static void set(String key, Integer expiration, Object object) {
        if (expiration == null) {
            tKillCacheUtils.setEx(key, object);
        } else {
            tKillCacheUtils.set(key, expiration, object);
        }
    }

//    /**
//     * 设置库存
//     *
//     * @param secId
//     * @param value
//     */
//    public static void setStock(long secId, String value) {
//        try {
//            String key = CacheConstant.CacheKey.getStockKey(secId);
//            tKillCacheUtils.set(key, CacheConstant.Expiration.ONE_HOUR, value);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 获取库存
//     *
//     * @param secId
//     * @return
//     */
//    public static String getStock(long secId) {
//        String key = CacheConstant.CacheKey.getStockKey(secId);
//        String stock = null;
//        try {
//            stock = tKillCacheUtils.getStr(key);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        return stock;
//    }
//
//    /**
//     * 获取秒杀商品
//     *
//     * @param secId
//     * @return
//     */
//    public static Seckill getKill(long secId) {
//        String key = CacheConstant.CacheKey.getKillKey(secId);
//        Seckill seckill = new Seckill();
//        try {
//            seckill = (Seckill) tKillCacheUtils.get(key);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        return seckill;
//    }
//
//    /**
//     * 缓存Seckill对象
//     *
//     * @param seckill
//     * @return
//     */
//    public void setKill(Seckill seckill) {
//        try {
//            String key = CacheConstant.CacheKey.getKillKey(seckill.getSeckillId());
//            tKillCacheUtils.set(key, CacheConstant.Expiration.TEN_MINUTES, seckill);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }

    /**
     * 将token push进jimdb队列
     */
    public static void pushToken(long seckillId, String token) {
        try {
            String key = CacheConstant.CacheKey.getTokenKey(seckillId);
            tKillCacheUtils.push(key, token);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 将token pop出jimdb队列
     */
    public static String popToken(long seckillId) {
        try {
            String key = CacheConstant.CacheKey.getTokenKey(seckillId);
            return tKillCacheUtils.pop(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * 清空key
     */
    public static void del(String... key) {
        try {
            tKillCacheUtils.delete(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static void setTokenSet(long seckillId, String token) {
        try {
            String key = CacheConstant.CacheKey.getTokenSetKey(seckillId);
            tKillCacheUtils.singleAdd(key, token);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static boolean verifyToken(String token, long seckillId) {

        try {
            String key = CacheConstant.CacheKey.getTokenSetKey(seckillId);
            return tKillCacheUtils.isMember(key, token);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


    public static void settKillCacheUtils(TKillCacheUtils tKillCacheUtils) {
        KillCacheTemplate.tKillCacheUtils = tKillCacheUtils;
    }

    /**
     * 数据库操作接口定义
     */
    public static interface KillCacheCallback {
        /**
         * @return
         */
        Object doGet() throws Exception;
    }

}
