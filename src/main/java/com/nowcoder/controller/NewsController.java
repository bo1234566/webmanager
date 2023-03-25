package com.nowcoder.controller;

import com.nowcoder.aspect.LogAspect;
import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Controller


public class NewsController {
    @Autowired
    NewsService newsservice;
    @Autowired
    QiniuService qiniuService;
    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    LikeService likeService;
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @RequestMapping(path = {"/uploadImage/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
//            String fileUrl_base = newsservice.saveImage(file);
            String fileUrl = qiniuService.saveImage(file);
            if (fileUrl == null) {
                return ToutiaoUtil.getJSONString(1, "上传图片失败");
            }
            return ToutiaoUtil.getJSONString(0, fileUrl);
        } catch (Exception e) {
            logger.error("上传图片失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "插入图片失败");
        }
    }

    /**
     * use for 127 domain image display--
     */
    @RequestMapping(path = {"/image"}, method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name") String imageName,
                         HttpServletResponse response) {
        try {
            response.setContentType("image/jpeg");
            StreamUtils.copy(new FileInputStream(new
                    File(ToutiaoUtil.IMAGE_DIR + imageName)), response.getOutputStream());
            logger.info("get image " + imageName);
        } catch (Exception e) {
            logger.error("读取图片错误" + e.getMessage());
        }
    }

    @RequestMapping(path = {"/user/addNews/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link) {
        try {
            String addResult = newsservice.addNews(image, title, link);
            if (StringUtils.isEmpty(addResult)) {
                logger.error("添加新闻失败");
                return ToutiaoUtil.getJSONString(1,"发布失败");
            } else {
                return ToutiaoUtil.getJSONString(0,"发布成功");
            }
        } catch (Exception e) {
            logger.error("news添加失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1,"发布失败");
        }
    }
    @RequestMapping(path = {"/news/{newsId}"}, method = {RequestMethod.GET})
    public String newsDetail(Model model, @PathVariable("newsId") int newsId){
        News news = newsservice.getById(newsId);
        if (news != null) {
            List<Comment> comments = commentService.getCommentsByEntity(news.getId(), EntityType.ENTITY_NEWS);
            List<ViewObject> commentVOs = new ArrayList<ViewObject>();
            if (news != null) {
                int localHost = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
                if (localHost != 0) {
                    model.addAttribute("like", likeService.getLikeStatus(localHost, EntityType.ENTITY_NEWS, newsId));
                } else {
                    model.addAttribute("like", 0);
                }
            }
            for (Comment comment : comments) {
                ViewObject commentVO = new ViewObject();
                commentVO.set("comment", comment);
                commentVO.set("user", userService.getUser(comment.getUserId()));
                commentVOs.add(commentVO);
            }
            model.addAttribute("comments", commentVOs);
        }
        model.addAttribute("news", news);
        model.addAttribute("owner",userService.getUser(news.getUserId()));
        return "detail";
    }

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("content") String content,
                          @RequestParam("newsId") int newsId) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);
            comment.setEntityType(EntityType.ENTITY_NEWS);
            comment.setEntityId(newsId);
            comment.setUserId(hostHolder.getUser().getId());
            commentService.addComment(comment);
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            newsservice.updateCommentCount(count, newsId);

        } catch (Exception e) {
            logger.error("插入评论失败" + e.getMessage());
        ToutiaoUtil.getJSONString(1,"添加评论失败");
        }
        return "redirect:/news/" + String.valueOf(newsId);
    }
    /**
     * todo delect comment function
     */

}
