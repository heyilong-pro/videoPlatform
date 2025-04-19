package com.lyy.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.lyy.config.AliyunConfig;
import com.lyy.pojo.Result;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class AliOssUtil {
    private    final static   String ACCESS_KEY_ID ;
    private   final static String ACCESS_KEY_SECRET ;

    // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
    private static final String ENDPOINT = "https://oss-cn-beijing.aliyuncs.com";
    // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
    //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

    // 填写Bucket名称，例如examplebucket。
    private static final String BUCKET_NAME = "big-event-heyilong";



    // 静态代码块，在类加载时执行
    static {
        Properties properties = new Properties();
        // 从类路径加载属性文件
        try (InputStream in = AliOssUtil.class.getClassLoader().getResourceAsStream("aliyun.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                throw new RuntimeException("Could not find aliyun.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load aliyun.properties", e);
        }

        // 从属性文件中获取值
        ACCESS_KEY_ID = properties.getProperty("aliyun.access.key-id");
        ACCESS_KEY_SECRET = properties.getProperty("aliyun.access.key-secret");

        // 简单验证
        if (ACCESS_KEY_ID == null || ACCESS_KEY_ID.isEmpty() ||
                ACCESS_KEY_SECRET == null || ACCESS_KEY_SECRET.isEmpty()) {
            throw new RuntimeException("OSS credentials not properly configured in aliyun.properties");
        }
    }


    public static String uploadFile(String objectName, InputStream in) throws Exception {

        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。

        // 填写Bucket所在地域。以华东1（杭州）为例，Region填写为cn-hangzhou。
        String region = "cn-beijing";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        String url = "";
        try {
            // 填写字符串。
            String content = "Hello OSS，你好世界";

            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, in);

            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传字符串。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            //url组成: https://bucket名称.区域节点/objectName
            url = "https://" + BUCKET_NAME + "." + ENDPOINT.substring(ENDPOINT.lastIndexOf("/") + 1) + "/" + objectName;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }

    // 删除文件
    public static void deleteFile(String objectKey) {
        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        ossClient.deleteObject(BUCKET_NAME, objectKey);

    }

    public static void deleteOssFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // 构造 OSS 文件前缀（如 https://my-bucket.oss-cn-shanghai.aliyuncs.com/）
        String ossPrefix = "https://" + BUCKET_NAME + "." + ENDPOINT.substring(ENDPOINT.lastIndexOf("/") + 1) + "/";

        if (fileUrl.startsWith(ossPrefix)) {
            // 提取 objectKey（如 videos/abc123.mp4）
            String objectKey = fileUrl.substring(ossPrefix.length());
            deleteFile(objectKey);
        } else {
            Result.error("OSS文件路径不匹配: " + fileUrl);
        }
    }
}
