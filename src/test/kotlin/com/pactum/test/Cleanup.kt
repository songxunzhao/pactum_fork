package com.pactum.test

import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD

@Sql(
  statements = [
    "DELETE FROM negotiation_field",
    "DELETE FROM negotiation",
    "DELETE FROM client_file",
    "DELETE FROM client",
    "DELETE FROM token"
  ],
  executionPhase = AFTER_TEST_METHOD
)
annotation class Cleanup
