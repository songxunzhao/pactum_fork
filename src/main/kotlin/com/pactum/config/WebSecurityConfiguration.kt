package com.pactum.config

import com.pactum.auth.model.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration(
  private val filter: AuthorizationFilter
) : WebSecurityConfigurerAdapter() {

  companion object {
    const val RESOURCE_REQUEST_MATCHER_BEAN = "resourceServerRequestMatcher"
    val PERMITTED_APIS = arrayOf(
      "/api/v1/chats/**",
      "/api/v1/models/**",
      "/api/v1/emails/**",
      "/api/v1/auth/login",
      "/api/v1/health/get"
    )

    val CLIENT_ACCESS_APIS = arrayOf(
      "/api/v1/auth/logout",
      "/api/v1/client/config",
      "/api/v1/client/file",
      "/api/v1/client/file/upload",
      "/api/v1/negotiation/client",
      "/api/v1/negotiation/summary/client"
    )
  }

  override fun configure(http: HttpSecurity) {

    http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .requestMatcher(resources()).authorizeRequests()
      .and()
      .csrf().disable()
      .addFilterAfter(filter, UsernamePasswordAuthenticationFilter::class.java)
      .authorizeRequests()
      .antMatchers(*PERMITTED_APIS).permitAll()
      .antMatchers(*CLIENT_ACCESS_APIS).hasAnyAuthority(Role.Admin.name, Role.Client.name)
      .anyRequest().hasAnyAuthority(Role.Admin.name)
  }

  @Bean(RESOURCE_REQUEST_MATCHER_BEAN)
  fun resources(): RequestMatcher {
    return AntPathRequestMatcher("/api/**")
  }
}
