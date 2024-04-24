package com.pactum.clientfile

import com.pactum.clientfile.model.ClientFile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientFileRepository : CrudRepository<ClientFile, Long> {
  fun findAllByClientId(clientId: Long): List<ClientFile>
}
