package com.pactum.config

import com.fasterxml.classmate.TypeResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.schema.AlternateTypeRules.newRule
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.lang.reflect.WildcardType

@Configuration
@EnableSwagger2
class SwaggerConfiguration(
  @Value("\${server.baseUrl}") val baseUrl: String
) {
  @Bean
  fun api(): Docket {
    val typeResolver = TypeResolver()
    return Docket(DocumentationType.SWAGGER_2)
      .alternateTypeRules(
        newRule(
          typeResolver.resolve(
            List::class.java,
            typeResolver.resolve(Map::class.java, String::class.java, Object::class.java)
          ),
          typeResolver.resolve(Map::class.java, String::class.java, WildcardType::class.java),
          Ordered.HIGHEST_PRECEDENCE
        )
      )
      .select()
      .apis(RequestHandlerSelectors.basePackage("com.pactum"))
      .paths(PathSelectors.any())
      .build()
      .apiInfo(
        ApiInfoBuilder()
          .title("Pactum API")
          .description("Automatically generated documentation for Pactum APIs")
          .version("1")
          .build()
      )
  }
}
