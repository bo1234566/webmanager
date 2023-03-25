package com.nowcoder.controller;

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
    @RequestMapping(path = {"/like"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String like(Model mode, @RequestParam("newsId") int newsId) {
        logger.info("start from like controller");
            User user = hostHolder.getUser();
        if (user != null) {
            long likeCount = likeService.like(user.getId(), EntityType.ENTITY_NEWS, newsId);
            newsService.updateLikeCount(newsId, (int) likeCount);
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
