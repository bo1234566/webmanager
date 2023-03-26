package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.controller.LoginController;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private ApplicationContext applicationContext;
    private Map<EventType, List<EventHandler>> config = new HashMap<>();

    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 初始化之后执行动作,所以重写aps,得到handler映射表
     * beans为EventHandler.CLASS中所有实现EventHandler的类
     * 把从beans中得到的实现类解析并存储在config的list
     * config 先生成eventType的list,然后add相关handler即注册handler
     * 在InitializingBean之后就启动线程执行 Event相关的处理逻辑
     * 根据从队列推出来的事件,找到对应的config映射表,执行handler方法(LikeHandler等)
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                for (EventType eventType : eventTypes) {
                    if (!config.containsKey(eventType)) {
                        config.put(eventType, new ArrayList<EventHandler>());
                    }
                    config.get(eventType).add(entry.getValue());
                    logger.info("config get  " +  config.get(eventType) + "add entry getValue" + entry.getValue());
                }
            }
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> messages = jedisAdapter.brpop(0, key);
                    for (String message : messages) {
                        if (message.equals(key)) {
                            logger.info("队列首个元素是队列名字 ?");
                            continue;
                        }
                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("非已定义事件");
                            continue;
                        }
                        for (EventHandler handler : config.get(eventModel)) {
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
