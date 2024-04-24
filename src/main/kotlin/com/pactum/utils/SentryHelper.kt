package com.pactum.utils

import io.sentry.Sentry

class SentryHelper {

  companion object {

    fun init(env: String?, release: String?) {
      Sentry.getStoredClient().environment = env
      Sentry.getStoredClient().release = release
    }

    fun report(ex: Exception, extras: Map<String, Any>) {
      Sentry.getStoredClient().extra = extras
      Sentry.getStoredClient().sendException(ex)
    }
  }
}
