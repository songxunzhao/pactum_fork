package com.pactum.test.fixture

import kotlin.reflect.KClass

annotation class WithAll(val value: Array<With>)

@Repeatable
@Suppress("DEPRECATED_JAVA_ANNOTATION")
@java.lang.annotation.Repeatable(WithAll::class)
annotation class With(
  val value: KClass<out TestFixture>
)
