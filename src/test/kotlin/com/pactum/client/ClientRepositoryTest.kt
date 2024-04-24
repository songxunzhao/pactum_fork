package com.pactum.client

import com.pactum.client.model.Client
import com.pactum.test.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class ClientRepositoryTest @Autowired constructor(
  private val repository: ClientRepository
) {

  @BeforeEach
  fun `clean up`() {
    repository.deleteAll()
  }

  @Test
  fun `finds client by tag`() {
    val tag = "tag"
    val name = "name"

    val client = Client.create(tag, name)
    val savedClient = repository.save(client)

    val foundClient = repository.findFirstByTag(tag)

    assertThat(foundClient).isEqualToComparingOnlyGivenFields(savedClient)
    assertThat(foundClient!!.name).isEqualTo(name)
  }

  @Test
  fun `finds all clients by tag except one`() {
    val tag1 = "tag1"
    val name1 = "name1"
    val tag2 = "tag2"
    val name2 = "name2"
    val client1 = Client.create(tag1, name1)
    repository.save(client1)
    val client2 = Client.create(tag2, name2)
    repository.save(client2)

    val foundClient = repository.findByTagNot(tag2)

    assertThat(foundClient.size).isEqualTo(1)
    assertThat(foundClient[0].name).isEqualTo(name1)
  }
}
