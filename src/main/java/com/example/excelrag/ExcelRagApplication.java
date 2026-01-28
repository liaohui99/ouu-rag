package com.example.excelrag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
   // QdrantVectorStoreAutoConfiguration.class
})
@MapperScan("com.example.excelrag.mapper")  // 替换为你的Mapper包路径
public class ExcelRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelRagApplication.class, args);
    }
}
