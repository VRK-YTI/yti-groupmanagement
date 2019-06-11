package fi.vm.yti.groupmanagement.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket frontendApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("0 public-api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/public-api/**"))
                .build();
    }

    @Bean
    public Docket reindexApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("1 internal-api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/api/**"))
                .build();
    }
   
/*     Removed this api from public view

    @Bean
    public Docket privateApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("2 private-api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/private-api/**"))
                .build();
    }

    @Bean
    public Docket frontEndApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("3 frontend-api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/api/**"))
                .build();
    }
*/    
}
