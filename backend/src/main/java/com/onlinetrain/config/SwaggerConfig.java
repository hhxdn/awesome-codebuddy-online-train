package com.onlinetrain.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Knife4j (Swagger) 配置
 */
@Configuration
@EnableSwagger2WebMvc
@EnableKnife4j
public class SwaggerConfig {

    @Bean
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.onlinetrain.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName("在线培训系统API");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("在线培训系统接口文档")
                .description("在线培训系统 RESTful API")
                .version("1.0.0")
                .contact(new Contact("OnlineTrain", "", ""))
                .build();
    }
}
