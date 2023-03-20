package com.nowcoder.service;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentDAO commentDAO;
    public List<Comment> getCommentsByEntity(int entityId,int EntityType) {
        return commentDAO.selectByEntity(entityId,EntityType);

    }
}
