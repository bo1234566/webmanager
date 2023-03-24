package com.nowcoder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis 配置说明
 * 首先下载redis releases:  https://github.com/microsoftarchive/redis/releases/tag/win-3.0.504
 * 解压后执行 redis-server redis.windows.conf
 *另外开cmd 窗口，执行 redis-server --service-install redis.windows-service.conf --loglevel verbose
 * 开启服务 redis-server --service-start
 * >redis-cli.exe -h 127.0.0.1 -p 6379（client端连接server端的redis）
 */
public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    public static void main(String[] args) {
        logger.info("start");
        Jedis jedis = new Jedis();
        jedis.set("hello", "100");
        jedis.incr("hello");
        JedisPool jedisPool = new JedisPool();
        for (int i = 0;i<50;i++) {
            Jedis j = jedisPool.getResource();
            j.get("a");
            logger.info("POOL" + i);
            j.close();
        }
    }
}
