package com.pactum.negotiationasset

import com.pactum.negotiationasset.model.NegotiationAsset
import com.pactum.negotiationasset.model.NegotiationAssetType
import com.pactum.test.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class NegotiationAssetRepositoryTest @Autowired constructor(
  private val repository: NegotiationAssetRepository
) {

  @Test
  fun `finds latest flow asset by drive id`() {
    val chatAsset = NegotiationAsset.newFlowAsset("1", 2, "3")
    val saved = repository.save(chatAsset)

    val found = repository.findLatestByDriveId("1", NegotiationAssetType.FLOW.name)
    assertThat(found).isNotNull()
    assertThat(found?.id).isEqualTo(saved.id)
    assertThat(repository.findLatestByDriveId("2", NegotiationAssetType.FLOW.name)).isNull()
    assertThat(repository.findLatestByDriveId("2", NegotiationAssetType.MODEL.name)).isNull()
    assertThat(repository.findLatestByDriveId("2", "invalidtype")).isNull()
  }

  @Test
  fun `finds latest model asset by drive id`() {
    val chatAsset = NegotiationAsset.newModelAsset("1", 2, "3")
    val saved = repository.save(chatAsset)

    val found = repository.findLatestByDriveId("1", NegotiationAssetType.MODEL.name)
    assertThat(found).isNotNull()
    assertThat(found?.id).isEqualTo(saved.id)
    assertThat(repository.findLatestByDriveId("2", NegotiationAssetType.MODEL.name)).isNull()
    assertThat(repository.findLatestByDriveId("2", NegotiationAssetType.FLOW.name)).isNull()
    assertThat(repository.findLatestByDriveId("2", "invalidtype")).isNull()
  }

  @Test
  fun `can resolve insert exception`() {
    val chatAsset = NegotiationAsset.newModelAsset("1", 2, "3")
    repository.save(chatAsset)

    val found = repository.resolveInsertException()
    assertThat(found).isNotNull()
  }
}
