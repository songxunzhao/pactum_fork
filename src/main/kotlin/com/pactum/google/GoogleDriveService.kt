package com.pactum.google

import com.google.api.services.drive.Drive
import com.pactum.config.Loggable
import com.pactum.utils.trace
import io.micrometer.core.annotation.Timed
import io.opentracing.Span
import io.opentracing.util.GlobalTracer
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

private val logger = logger {}

@Service
class GoogleDriveService(
  private val googleDrive: Drive,
  @Qualifier("asyncExecutor") private val executor: Executor,
  @Value("\${google.drive.metadataTimeout}") private var metadataTimeout: Long,
  @Value("\${google.drive.contentTimeout}") private var contentTimeout: Long
) {

  companion object {
    const val FILE_CACHE = "file"
  }

  @Timed("google_drive.get_content")
  @Loggable(includeArgs = true)
  @Cacheable(value = [FILE_CACHE], key = "#fileId")
  fun getContent(fileId: String): String {
    val tracer = GlobalTracer.get()
    return tracer.trace("Google_Drive_getContent") { span: Span ->
      span.setTag("file_id", fileId)

      val run = Callable {
        return@Callable googleDrive.files().get(fileId).executeMediaAsInputStream().bufferedReader().use { it.readText() }
      }
      val future = FutureTask(run)
      executor.execute(future)
      try {
        future[contentTimeout, TimeUnit.MILLISECONDS]
      } catch (e: Exception) {
        logger.error(e.localizedMessage, e)
        future.cancel(true)
        throw e
      }
    } as String
  }

  @Timed("google_drive.get_md5_checksum")
  @Loggable(includeArgs = true, includeResults = true)
  fun getMD5Checksum(fileId: String): String {
    val tracer = GlobalTracer.get()
    return tracer.trace("Google_Drive_md5CheckSum") { span: Span ->
      span.setTag("file_id", fileId)

      val run = Callable {
        return@Callable googleDrive.files().get(fileId).setFields("md5Checksum").execute().md5Checksum
      }
      val future = FutureTask(run)
      executor.execute(future)
      try {
        future[metadataTimeout, TimeUnit.MILLISECONDS]
      } catch (e: Exception) {
        logger.error(e.localizedMessage, e)
        future.cancel(true)
        throw e
      }
    } as String
  }
}
