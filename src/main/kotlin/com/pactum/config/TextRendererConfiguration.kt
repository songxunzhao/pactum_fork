package com.pactum.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.xhtmlrenderer.pdf.ITextRenderer

@Configuration
class TextRendererConfiguration {

  @Bean
  fun rendererResolver(): ITextRenderer {
    return ITextRenderer()
  }
}
