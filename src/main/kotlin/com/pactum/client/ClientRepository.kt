package com.pactum.client

import com.pactum.client.model.Client
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : CrudRepository<Client, Long> {

  @Query("SELECT * FROM client WHERE tag = :tag LIMIT 1")
  fun findFirstByTag(tag: String): Client?

  @Query("SELECT * FROM client WHERE tag != :tag")
  fun findByTagNot(tag: String): List<Client>
}
