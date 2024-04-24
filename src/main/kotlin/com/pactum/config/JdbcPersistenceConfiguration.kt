package com.pactum.config

import com.pactum.Application
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.data.auditing.CurrentDateTimeProvider
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.util.Optional

@Configuration
@EnableJdbcRepositories(basePackageClasses = [Application::class])
@EnableJdbcAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "dateTimeProvider")
class JdbcPersistenceConfiguration : AbstractJdbcConfiguration() {

  @Bean
  fun auditorAware() = AuditorAware<String> {
    val principal = SecurityContextHolder.getContext()?.authentication?.principal
    if (principal is User) {
      Optional.of(principal.username)
    } else {
      Optional.empty()
    }
  }

  @Bean
  @Profile("!test")
  fun dateTimeProvider() = CurrentDateTimeProvider.INSTANCE

  @Bean
  override fun jdbcCustomConversions(): JdbcCustomConversions {
    return JdbcCustomConversions(listOf(PGobjectStringConverter(), HstoreStringConverter()))
  }

  @ReadingConverter
  class PGobjectStringConverter : Converter<PGobject, String> {
    override fun convert(source: PGobject): String? {
      return if (source.type == "json" || source.type == "jsonb") {
        source.toString()
      } else {
        throw IllegalArgumentException("Type ${source.type} not supported!")
      }
    }
  }

  @ReadingConverter
  class HstoreStringConverter : Converter<Map<String, String>, String> {
    override fun convert(source: Map<String, String>): String {
      return source.map { e -> "${e.key}=>${e.value}" }.joinToString()
    }
  }
}
