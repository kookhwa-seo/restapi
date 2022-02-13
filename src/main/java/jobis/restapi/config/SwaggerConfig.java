package jobis.restapi.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private static final Contact DEFAULT_CONTACT = new Contact("kookhwa seo",
            "", "kookhwa836@gmail.com");

    private static final ApiInfo DEFAULT_API_INFO = new ApiInfo("REST API",
            "삼쩜삼 사용자 API", "1.0", "Terms of service",
            DEFAULT_CONTACT, "",
            "");

    private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<>(
            Arrays.asList("application/json"));

    private ApiKey apiKey()
    {
        return new ApiKey("Authorization", "Authorization", "header");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/szs/**"))
                .build()
                .apiInfo(DEFAULT_API_INFO)
                .securitySchemes(Lists.newArrayList(apiKey()));
    }
}
