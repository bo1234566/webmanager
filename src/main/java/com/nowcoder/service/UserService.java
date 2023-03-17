package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by nowcoder on 2023/03/06.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    public Map<String, Object> register(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isBlank(username)) {
            map.put("msgname", "用户名不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msgpwd", "密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);
        logger.info("selectByName"+user);
        if (user != null) {
            map.put("msgname", "用户名已被注册");
            return map;
        }
        //todo 敏感词
        if(username.equals("admin")){
            map.put("msgname","用户名不合法");
            return map;
        }
        // 密码强度
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        user.setHeadUrl(head);
        user.setPassword(ToutiaoUtil.MD5(password+user.getSalt()));
        userDAO.addUser(user);

        // 登陆
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;
    }


    public Map<String, Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        logger.info("user logger come!!" );
        if (StringUtils.isBlank(username)) {
            map.put("msgname", "用户名不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msgpwd", "密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);

        if (user == null) {
            map.put("msgname", "用户名不存在");
            return map;
        }

        if (!ToutiaoUtil.MD5(password+user.getSalt()).equals(user.getPassword())) {
            map.put("msgpwd", "密码不正确");
            return map;
        }

        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;
    }
    private String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        Date date = new Date();
        //ticket expired time count
        date.setTime(date.getTime() +1000*60*10);
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addTicket(ticket);
        logger.info("user id add ticket " + userId);
        logger.info("addTicket "+ticket.getTicket());
        return ticket.getTicket();
    }

    public User getUser(int id) {
        return userDAO.selectById(id);
    }

    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);
    }
    public Map<String, Object> forgetPassword(String username, String oldPassword, String newPassword){
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isBlank(username)) {
            map.put("msgname", "用户名不能为空");
            return map;
        }
        User user = userDAO.selectByName(username);
        if(user == null){
            map.put("msgname", "该用户名不存在，请重试");
            return map;
        }
        String dataBasePassword = user.getPassword();
        String userBasePassword = ToutiaoUtil.MD5(oldPassword + user.getSalt());
        String userNewPassword = ToutiaoUtil.MD5(newPassword + user.getSalt());
        if(!StringUtils.equals(dataBasePassword, userBasePassword))
        {
            map.put("msgpwd", "原始密码不正确");
            return map;
        }
        else if(StringUtils.equals(oldPassword, newPassword)){
            map.put("msgpwd", "新密码不能和原密码相同");
            return map;
        }
        else if(StringUtils.equals(dataBasePassword, userNewPassword)){
            map.put("msgpwd", "新密码不能与近期密码一样");
            return map;
        }
        user.setPassword(userNewPassword);
        userDAO.updatePassword(user);
        logger.info("更新用户新密码成功 " +  newPassword);
        return map;
    }
}
