package com.bdaim.common.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**  
 * redis工具类
 *   
 * @author chengning@salescomm.net
 * @date 2019/8/22 16:05
 */
@Component
public class RedisUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RedisUtil.class);

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取指定key的值,如果key不存在返回null，如果该Key存储的不是字符串，会抛出一个错误
     *
     * @param key
     * @return
     */
    public String get(String key) {
        String value = null;
        try {
            value = String.valueOf(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            LOG.error("获取指定key的值异常,", e);
        }
        return value;
    }

    /**
     * 设置key的值为value
     *
     * @param key
     * @param value
     * @return
     */
    public String set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return "ok";
        } catch (Exception e) {
            LOG.error("设置key的值异常,", e);
            return "fail";
        }
    }

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param keys
     * @return
     */
    public Long del(List keys) {
        Long result = null;
        try {
            result = redisTemplate.delete(keys);
        } catch (Exception e) {
            LOG.error("删除key异常,", e);
        }
        return result;
    }

    public boolean del(String key) {
        boolean result = false;
        try {
            result = redisTemplate.delete(key);
        } catch (Exception e) {
            LOG.error("删除key异常,", e);
        }
        return result;
    }

    /**
     * 通过key向指定的value值追加值
     *
     * @param key
     * @param str
     * @return
     */
    public Integer append(String key, String str) {
        return redisTemplate.opsForValue().append(key, str);
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置key value并指定这个键值的有效期
     *
     * @param key
     * @param seconds
     * @param value
     * @return
     */
    public void setex(String key, int seconds, String value) {
        try {
            redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("设置key value并指定这个键值的有效期异常,", e);
        }
    }

    public boolean batchSet(Map<String, String> map) {
        try {
            final RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            redisTemplate.executePipelined((RedisCallback<String>) conn -> {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    conn.set(entry.getKey().getBytes(), entry.getValue().getBytes());
                }
                return null;
            }, serializer);
        } catch (Exception e) {
            LOG.info("使用管道操作出错:{}", e.getMessage());
            return false;
        }
        return true;
    }

}
