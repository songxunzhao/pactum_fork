package com.pactum.config

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.ViewResolver
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.spring5.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode.HTML
import org.thymeleaf.templateresolver.ITemplateResolver

@Configuration
@AutoConfigureBefore(ThymeleafAutoConfiguration::class)
class TemplateEngineConfiguration {

  @Bean
  fun templateResolver(applicationContext: ApplicationContext): ITemplateResolver {
    return SpringResourceTemplateResolver().apply {
      setApplicationContext(applicationContext)
      prefix = "classpath:/templates/"
      suffix = ".html"
      templateMode = HTML
      characterEncoding = "UTF-8"
    }
  }

  @Bean
  fun templateEngine(
    templateResolver: ITemplateResolver,
    messageSource: ReloadableResourceBundleMessageSource
  ): SpringTemplateEngine {
    return SpringTemplateEngine().apply {
      enableSpringELCompiler = true
      setTemplateResolver(templateResolver)
      setTemplateEngineMessageSource(messageSource)
    }
  }

  @Bean
  fun viewResolver(engine: SpringTemplateEngine): ViewResolver {
    return ThymeleafViewResolver().apply {
      templateEngine = engine
      order = 1
    }
  }

  @Bean
  fun messageSource(): ReloadableResourceBundleMessageSource {
    return ReloadableResourceBundleMessageSource().apply {
      setBasename("classpath:lang/messages")
    }
  }
}
