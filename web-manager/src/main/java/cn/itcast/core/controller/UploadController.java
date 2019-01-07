package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) throws Exception {
        try {
           FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            String s = fastDFSClient.uploadFile(file.getBytes(), file.getOriginalFilename(), file.getSize());
            return new Result(true, FILE_SERVER_URL + s);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上穿失败");
        }
    }
}
