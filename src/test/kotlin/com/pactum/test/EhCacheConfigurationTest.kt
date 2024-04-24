package com.pactum.test

import com.pactum.google.GoogleDriveService
import net.sf.ehcache.Cache
import net.sf.ehcache.config.CacheConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableCaching
@Profile("test")
class EhCacheConfigurationTest {

  @Bean
  fun testCacheFactory(): EhCacheManagerFactoryBean {
    return EhCacheManagerFactoryBean().apply {
      setAcceptExisting(true)
    }
  }

  @Bean
  fun testCacheManager(cacheFactory: EhCacheManagerFactoryBean): EhCacheCacheManager {
    val fileCacheConfig = CacheConfiguration()
      .name(GoogleDriveService.FILE_CACHE)
      .timeToLiveSeconds(5)
      .maxEntriesLocalHeap(100)
      .memoryStoreEvictionPolicy("LRU")
    val fileCache = Cache(fileCacheConfig)
    cacheFactory.getObject()!!.removeAllCaches()
    cacheFactory.getObject()!!.addCache(fileCache)
    return EhCacheCacheManager(cacheFactory.getObject()!!)
  }
}
