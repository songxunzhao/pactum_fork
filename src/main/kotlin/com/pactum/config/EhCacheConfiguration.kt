package com.pactum.config

import com.pactum.google.GoogleDriveService
import net.sf.ehcache.Cache
import net.sf.ehcache.config.CacheConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableCaching
@Profile("!test")
class EhCacheConfiguration(
  @Value("\${google.drive.cacheTime}") val driveCacheTime: Long
) {

  @Bean
  fun realCacheFactory(): EhCacheManagerFactoryBean {
    return EhCacheManagerFactoryBean()
  }

  @Bean
  fun realCacheManager(cacheFactory: EhCacheManagerFactoryBean): EhCacheCacheManager {
    val fileCacheConfig = CacheConfiguration()
      .name(GoogleDriveService.FILE_CACHE)
      .timeToLiveSeconds(driveCacheTime)
      .maxEntriesLocalHeap(100)
      .memoryStoreEvictionPolicy("LRU")
    val fileCache = Cache(fileCacheConfig)
    cacheFactory.getObject()!!.addCache(fileCache)
    return EhCacheCacheManager(cacheFactory.getObject()!!)
  }
}
