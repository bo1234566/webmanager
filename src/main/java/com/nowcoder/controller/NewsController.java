package com.nowcoder.controller;

import com.nowcoder.aspect.LogAspect;
import com.nowcoder.model.News;
import com.nowcoder.model.User;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.QiniuService;
import com.nowcoder.service.UserService;
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

@Controller


public class NewsController {
    @Autowired
    NewsService newsservice;
    @Autowired
    QiniuService qiniuService;
    @Autowired
    UserService userService;
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
        if(news != null){

        }
        model.addAttribute("news", news);
        model.addAttribute("owner",userService.getUser(news.getUserId()));
        return "detail";
    }

}
