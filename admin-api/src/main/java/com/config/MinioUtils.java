package com.config;

import com.util.StringUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MinioUtils {

    @Autowired
    private MinioClient minioClient;


    /**
     * 判断桶是否存在
     */
    @SneakyThrows(Exception.class)
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }


    /**
     * 创建桶
     * @param bucketName
     * 获取全部的桶  minioClient.listBuckets();
     */
    @SneakyThrows(Exception.class)
    public void createBucket(String bucketName) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }


    /**
     * 根据bucketName获取信息
     * @param bucketName bucket名称
     */
    @SneakyThrows(Exception.class)
    public Optional<Bucket> getBucket(String bucketName) {
        return minioClient.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }

    /**
     * 根据bucketName删除信息
     * @param bucketName bucket名称
     */
    @SneakyThrows(Exception.class)
    public  void removeBucket(String bucketName) {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取文件流
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    @SneakyThrows(Exception.class)
    public InputStream getObject(String bucketName, String objectName) {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }


    /**
     * 上传本地文件
     * @param bucketName 存储桶
     * @param objectName 对象名称
     * @param fileName   本地文件路径
     */
    @SneakyThrows(Exception.class)
    public  ObjectWriteResponse putObject(String bucketName, String objectName, String fileName) {
        if(!bucketExists(bucketName)){
            createBucket(bucketName);
        }
        return minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
    }

    /**
     * 通过流上传文件
     * @param bucketName  存储桶
     * @param objectName  文件对象
     * @param inputStream 文件流
     */
    @SneakyThrows(Exception.class)
    public ObjectWriteResponse putObject(String bucketName, String objectName, InputStream inputStream) {
        if(!bucketExists(bucketName)){
            createBucket(bucketName);
        }
        return minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(inputStream, inputStream.available(), -1).build());
    }


    /**
     * 單個文件上傳遞
     * @param bucketName
     * @param multipartFile
     * @return
     */
    @SneakyThrows(Exception.class)
    public String uploadFileSingle(String bucketName, MultipartFile multipartFile ) {
        if(!bucketExists(bucketName)){
            createBucket(bucketName);
        }
        String fileName = multipartFile.getOriginalFilename();
        String[] split = fileName.split("\\.");
        if (split.length > 1) {
            fileName = split[0] + "." + split[1];
        } else {
            fileName = fileName + System.currentTimeMillis();
        }
        InputStream in = null;
        try {
            in = multipartFile.getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(in, in.available(), -1).contentType(multipartFile.getContentType()).build());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return getUploadObjectUrl(bucketName,fileName, 7*24*60*60);
    }


    /**
     * description: 上传文件
     * @param multipartFile
     * @return: java.lang.String
     */
    public List<String> uploadFileBatch(String  bucketName, MultipartFile[] multipartFile) {
        if(!bucketExists(bucketName)){
            createBucket(bucketName);
        }
        List<String> names = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            try {
                String fileName = file.getOriginalFilename();
                uploadFileSingle(bucketName,file);
                names.add(fileName);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return names;
    }


    /**
     * 获取文件外链
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires    过期时间 <=7 秒级
     * @return url
     */
    @SneakyThrows(Exception.class)
    public String getUploadObjectUrl(String bucketName, String objectName, Integer expires)  {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.PUT).bucket(bucketName).object(objectName).expiry(expires).build());
    }

    /**
     * 文件下载
     * @param fileName 文件名
     * @param delete   是否删除
     * @throws IOException
     */
    public void fileDownload(@RequestParam(name = "fileName") String fileName,
                             @RequestParam(defaultValue = "false") Boolean delete,
                             HttpServletResponse response, String bucketName) {

        InputStream inputStream   = null;
        OutputStream outputStream = null;
        try {
            if (StringUtil.isNull(fileName)) {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                String data = "文件下载失败";
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes("UTF-8"));
                return;
            }

            outputStream = response.getOutputStream();
            // 获取文件对象
            inputStream =minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
            byte buf[] = new byte[1024];
            int length = 0;
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName.substring(fileName.lastIndexOf("/") + 1), "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            // 输出文件
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            inputStream.close();
            // 判断：下载后是否同时删除minio上的存储文件
            if (delete) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            }
        } catch (Throwable ex) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            try {
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes("UTF-8"));
            }catch (IOException e){
                e.printStackTrace();
            }
        } finally {
            try {
                outputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }}catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
