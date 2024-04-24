package com.pactum.clientfile

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericOkResponse
import com.pactum.clientfile.model.ClientFile
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ClientFileController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class ClientFileControllerTest {
  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var clientFileService: ClientFileService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun upload() {
    val mockFile = MockMultipartFile("file", byteArrayOf(1, 2, 3, 4))
    val passphrase = "mypass"

    val clientFile: ClientFile.ApiEntity = mock()
    whenever(clientFileService.processUploadedFile(mockFile, passphrase)).thenReturn(GenericCreatedResponse(clientFile))

    mockMvc.perform(
      multipart("/api/v1/client/file/upload")
        .file(mockFile)
        .param("passphrase", passphrase)
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isCreated)
      .andExpect(content().string(jacksonObjectMapper().writeValueAsString(clientFile)))
  }

  @Test
  @WithMockUser
  fun getFiles() {
    val clientFiles: List<ClientFile.ApiEntity> = listOf(mock(), mock())
    whenever(clientFileService.getFiles()).thenReturn(GenericOkResponse(clientFiles))

    mockMvc.perform(get("/api/v1/client/file"))
      .andExpect(status().isOk)
      .andExpect(content().string(jacksonObjectMapper().writeValueAsString(clientFiles)))
  }
}
