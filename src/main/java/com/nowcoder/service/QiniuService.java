package com.nowcoder.service;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.nowcoder.aspect.LogAspect;
import com.nowcoder.util.ToutiaoUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.Region;
import com.qiniu.storage.model.DefaultPutRet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.storage.Configuration;
import com.qiniu.http.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class QiniuService {
    private static final Logger logger = LoggerFactory.getLogger(QiniuService.class);
    String accessKey = "SJ-fW4E0lyU7RaKdEArj47DhTFyy6IzS8n7v0Qea";
    String secretKey = "p9VzFtVcKp3kJzD28rxtz841E7V1XS4PZknmKnoX";
    String bucket = "webmanager";

    private static String QINIU_IMAGE_DOMAIN_HuaDong_ZheJiang = "http://rrpu1mv38.bkt.clouddn.com/";

    private static String QINIU_IMAGE_DOMAIN_HuaBei = "http://img.webcloud123.top/";

    //构造一个带指定 Region 对象的配置类
//    Configuration cfg = new Configuration(Region.huadongZheJiang2());
    Configuration cfg = new Configuration(Region.region1());
//    cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
    //...其他参数参考类注释
    UploadManager uploadManager = new UploadManager(cfg);
    //...生成上传凭证，然后准备上传

    Auth auth = Auth.create(accessKey, secretKey);
    String upToken = auth.uploadToken(bucket);

    public String saveImage(MultipartFile file) throws IOException {
        int dotPos = file.getOriginalFilename().lastIndexOf(".");
        if (dotPos < 0) {
            return null;
        }
        String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
        if (!ToutiaoUtil.isFileAllowed(fileExt)) {
            return null;
        }

        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;
        logger.info("uuid_filename is " + fileName);
            //调用put方法上传
            try {
                Response res =  uploadManager.put(file.getBytes(),fileName,upToken);
                return QINIU_IMAGE_DOMAIN_HuaBei + JSONObject.parseObject(res.bodyString()).get("key");
            } catch (QiniuException e) {
                logger.error("上传图片失败" + e.getMessage());
                throw new RuntimeException(e);

            }

    }
}
