package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        if (jedisAdapter.sIsmember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        String disLikeKey = RedisKeyUtil.getDislikeKey(entityType, entityId);
        if (jedisAdapter.sIsmember(disLikeKey, String.valueOf(userId))) {
            return -1;
        }
        return 0;
    }

    public long like(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sAdd(likeKey, String.valueOf(userId));
        String disLikeKey = RedisKeyUtil.getDislikeKey(entityType, entityId);
        jedisAdapter.sRem(disLikeKey, String.valueOf(userId));
        return jedisAdapter.sCard(likeKey);
    }
    public long disLike(int userId, int entityType, int entityId) {
        String disLikeKey = RedisKeyUtil.getDislikeKey(entityType, entityId);
        jedisAdapter.sAdd(disLikeKey, String.valueOf(userId));
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sRem(likeKey, String.valueOf(userId));
        return jedisAdapter.sCard(likeKey);
    }
}
