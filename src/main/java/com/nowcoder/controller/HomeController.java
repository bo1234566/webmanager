package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.News;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bo1234566 on 2023/3/12.
 */
@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    /**
     * getNews负责整体显示和单独用户显示
     * 通过vos传递给前端参数
     * 统一显示home.html界面
     */
    private List<ViewObject> getNews(int userId, int offset, int limit) {
        List<News> newsList = newsService.getLatestNews(userId, offset, limit);

        List<ViewObject> vos = new ArrayList<>();
        for (News news : newsList) {
            ViewObject vo = new ViewObject();
            vo.set("news", news);
            vo.set("user", userService.getUser(news.getUserId()));
            vos.add(vo);
        }
        return vos;
    }

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model,
                        @RequestParam(value = "pop", defaultValue = "0") int pop) {
        model.addAttribute("vos", getNews(0, 0, 100));
        model.addAttribute("pop", pop);
        logger.info("test: this is / or /index rendering");
        return "home";
    }
/*
* model.addAttribute的对象 vos 传递给home.html
 */
    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId) {
        model.addAttribute("vos", getNews(userId, 0, 100));
        return "home";
    }
    /**
     * todo forgetPassword function
     *
     */
    @RequestMapping(path = {"/forgetPassword/"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String forgetPassword(Model model, @RequestParam("username") String username,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @CookieValue("ticket") String ticket){
        try {
            Map<String, Object> map = userService.forgetPassword(username, oldPassword, newPassword);
            if (map.isEmpty()) {
                model.addAttribute("vos",getNews(0,0,100));
                logger.info("test: this is /forgetPassword/ rendering");
                userService.logout(ticket);
                return "home";
            } else {
                return ToutiaoUtil.getJSONString(1, map.values().toString());
            }
        }
        catch (Exception e){
            logger.error("更新密码失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1,"更新密码失败");
        }
    }
}
