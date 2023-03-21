package com.nowcoder.service;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.dao.NewsDAO;
import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.News;
import com.nowcoder.util.ToutiaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by bo1234566 on 2023/03/07.
 */
@Service
public class NewsService {
    @Autowired
    private NewsDAO newsDAO;
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentDAO commentDAO;
    public List<News> getLatestNews(int userId, int offset, int limit) {
        return newsDAO.selectByUserIdAndOffset(userId, offset, limit);
    }

    /**
     * news add to local path
     * @param file
     * @return
     * @throws IOException
     */
    public String saveImage(MultipartFile file) throws IOException {
        int dotPos = file.getOriginalFilename().lastIndexOf(".");
        if(dotPos < 0){
            return null;
        }
        String fileExt = file.getOriginalFilename().substring(dotPos+1).toLowerCase();
        if(!ToutiaoUtil.isFileAllowed(fileExt)){
            return null;
        }
        String fileName = UUID.randomUUID().toString().replaceAll("-","") + "." + fileExt;
        Files.copy(file.getInputStream(),new File(ToutiaoUtil.IMAGE_DIR + fileName).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        return ToutiaoUtil.TOUTIAO_DOMAIN+ "image?name=" + fileName;
    }
    public String addNews(String image, String title, String link){
        News news = new News();
        if(hostHolder.getUser() != null){
            news.setUserId(hostHolder.getUser().getId());
        } else{
            //“匿名id”
            news.setUserId(3);
        }

        news.setImage(image);
        news.setTitle(title);
        news.setCreatedDate(new Date());
        news.setLink(link);
        newsDAO.addNews(news);
        return ToutiaoUtil.getJSONString(0,"添加news成功");
    }

    public void updateCommentCount(int commentCount, int newsId) {
        newsDAO.updateCommentCount(commentCount, newsId);
    }

    public News getById(int newsId){
       return newsDAO.selectById(newsId);
    }
}
