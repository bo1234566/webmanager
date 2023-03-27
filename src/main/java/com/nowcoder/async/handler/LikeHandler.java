package com.nowcoder.async.handler;
import com.nowcoder.async.EventConsumer;
import com.nowcoder.async.EventType;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(LikeHandler.class);
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;
    @Override
    public void doHandle(EventModel model) {
        logger.info("program act as do likeHandler");
        Message message = new Message();
        User user = userService.getUser(model.getActorId());
        message.setToId(model.getEntityOwnerId());
        message.setContent("用户" + user.getName() +
                "点赞了你的资讯,http://127.0.0.1:8080/news/"
                + String.valueOf(model.getEntityId()));
        //此处不应该是userName,以和user发的站内信区别.使用系统账号,3
        message.setFromId(3);
        message.setCreatedDate(new Date());
        int fromId = 3;
        int toId = model.getEntityOwnerId();
        String conversationId = fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId);
        message.setConversationId(conversationId);
        messageService.addMessage(message);
    }


    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
