package com.fangw.simplemall.product;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.service.BrandService;

@SpringBootTest
class SimplemallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Resource
    OSSClient ossClient;

    @Test
    void contextLoads() {
        // BrandEntity brandEntity = new BrandEntity();
        //
        // brandEntity.setDescript("");
        // brandEntity.setName("huawei");
        // brandService.save(brandEntity);
        //
        // System.out.println("Insert succeed.");

        List<BrandEntity> brandId = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        brandId.forEach(brand -> {
            System.out.println(brand);
        });
    }

    @Test
    void aliYunUploadTest() {

        // // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        // String endpoint = "oss-cn-nanjing.aliyuncs.com";
        // // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        // String accessKeyId = "";
        // String accessKeySecret = "";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "simplemall";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "huawei.jpg";
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        String filePath = "F:\\尚硅谷\\谷粒商城\\Guli Mall\\课件和文档(老版)\\基础篇\\资料\\pics\\1f15cdbcf9e1273c.jpg";

        // 创建OSSClient实例。
        // OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = new FileInputStream(filePath);
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
