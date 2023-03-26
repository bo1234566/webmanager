package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.User;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.NewsService;
import com.nowcoder.util.RedisUtil;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {
    private static final Logger logger = LoggerFactory.getLogger(LikeController.class);
    @Autowired
    HostHolder hostHolder;
    @Autowired
    LikeService likeService;
    @Autowired
    NewsService newsService;
    @Autowired
    EventProducer eventProducer;

    /**
     * 设置异步发送参数,传入event队列
     * @param mode
     * @param newsId
     * @return
     */
    @RequestMapping(path = {"/like"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String like(Model mode, @RequestParam("newsId") int newsId) {
        logger.info("start from like controller");
            User user = hostHolder.getUser();
        if (user != null) {
            long likeCount = likeService.like(user.getId(), EntityType.ENTITY_NEWS, newsId);
            newsService.updateLikeCount(newsId, (int) likeCount);
            //fire an event to likeHandler
            eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_NEWS)
                    .setEntityId(newsId).setEntityOwnerId(newsService.getById(newsId).getUserId()));
            return ToutiaoUtil.getJSONString(0, String.valueOf(likeCount));
        } else {
            return ToutiaoUtil.getJSONString(1, "点赞失败");
        }
    }
    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String dislike(Model mode, @RequestParam("newsId") int newsId) {
        User user = hostHolder.getUser();
        long likeCount = likeService.disLike(user.getId(), EntityType.ENTITY_NEWS, newsId);
        newsService.updateLikeCount(newsId, (int) likeCount);
        return ToutiaoUtil.getJSONString(0, String.valueOf(likeCount));
    }
}
