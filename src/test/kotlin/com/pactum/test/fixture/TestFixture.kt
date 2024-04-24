package com.pactum.test.fixture

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

interface TestFixture {
  fun apply(jdbcTemplate: NamedParameterJdbcTemplate)
}
