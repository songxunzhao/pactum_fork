package com.pactum.clientfile

import com.pactum.client.ClientRepository
import com.pactum.client.model.Client
import com.pactum.clientfile.model.ClientFile
import com.pactum.clientfile.model.createMock
import com.pactum.test.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class ClientFileRepositoryTest @Autowired constructor(
  private val clientRepository: ClientRepository,
  private val repository: ClientFileRepository
) {

  @Test
  fun `finds client files by client id`() {
    val client1 = Client.create("tag1", "user1")
    val client2 = Client.create("tag2", "user2")
    val savedClient1 = clientRepository.save(client1)
    val savedClient2 = clientRepository.save(client2)

    val clientFile1 = ClientFile.createMock(savedClient1.id!!, "file1")
    val clientFile2 = ClientFile.createMock(savedClient2.id!!, "file2")
    val clientFile3 = ClientFile.createMock(savedClient1.id!!, "file3")
    val savedClientFile1 = repository.save(clientFile1)
    repository.save(clientFile2)
    val savedClientFile3 = repository.save(clientFile3)

    val foundClientFiles = repository.findAllByClientId(savedClient1.id!!)

    assertThat(foundClientFiles).containsExactlyInAnyOrder(savedClientFile1, savedClientFile3)
  }
}
