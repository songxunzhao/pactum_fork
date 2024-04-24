package com.pactum.clientfile

import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericOkResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@Api(tags = ["admin"], description = "Secure file drop APIs consumed by the Admin panel")
class ClientFileController(
  private val clientFileService: ClientFileService
) {
  @ApiOperation(value = "Securely encrypt and store a client file")
  @PostMapping("/api/v1/client/file")
  fun upload(
    @RequestParam file: MultipartFile,
    @RequestParam passphrase: String
  ): GenericCreatedResponse {
    return clientFileService.processUploadedFile(file, passphrase)
  }

  @Deprecated("use POST api/v1/client/file")
  @ApiOperation(value = "Securely encrypt and store a client file (deprecated)")
  @PostMapping("/api/v1/client/file/upload")
  fun uploadDeprecated(
    @RequestParam file: MultipartFile,
    @RequestParam passphrase: String
  ): GenericCreatedResponse {
    return clientFileService.processUploadedFile(file, passphrase)
  }

  @ApiOperation(value = "Get list of uploaded client files' metadata")
  @GetMapping("/api/v1/client/file")
  fun getFiles(): GenericOkResponse {
    return clientFileService.getFiles()
  }
}
