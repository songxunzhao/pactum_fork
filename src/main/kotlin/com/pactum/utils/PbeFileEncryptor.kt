package com.pactum.utils

import org.springframework.stereotype.Service
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

const val PBE_ALGORITHM = "PBEWithHmacSHA512AndAES_256"
const val PBE_SALT_LENGTH = 8
const val PBE_HASH_ITERATION_COUNT = 100000

val OPENSSL_SALTED_PREFIX = "Salted__".toByteArray(Charsets.UTF_8)

@Service
class PbeFileEncryptor(
  private val secureRandom: SecureRandom
) {
  /*
    Returns file in openssl PBKDF2 with HMAC SHA-512 and AES256-CBC encryption format with IV added as first 16 bytes.

    To decrypt file, download it from google storage and run openssl command locally:
    $ IN=myfile OUT=myfile.out bash -c 'tail -c+17 $IN > enc.tmp &&\
      openssl enc -d -aes-256-cbc -md sha512 -iter 100000 -iv "$(head -c 16 $IN | xxd -ps -c 16 | tr -d '\n')" -in enc.tmp -out $OUT;\
      rm -r enc.tmp'

    and replace IN, OUT values with input and output file names. openssl will prompt to enter passphrase for decryption.
    Requires latest openssl 1.1.1, on macOS can install it using `brew install openssl`
   */
  fun encryptFile(fileContent: ByteArray, passphrase: String): ByteArray {
    val factory = SecretKeyFactory.getInstance(PBE_ALGORITHM)
    val keySpec = PBEKeySpec(passphrase.toCharArray())
    val key = factory.generateSecret(keySpec)

    val cipher = Cipher.getInstance(PBE_ALGORITHM)
    val salt = ByteArray(PBE_SALT_LENGTH)
    val iv = ByteArray(cipher.blockSize)
    secureRandom.nextBytes(salt)
    secureRandom.nextBytes(iv)

    val pbeSpec = PBEParameterSpec(salt, PBE_HASH_ITERATION_COUNT, IvParameterSpec(iv))
    cipher.init(Cipher.ENCRYPT_MODE, key, pbeSpec)
    val cipherText = cipher.doFinal(fileContent)

    return iv + OPENSSL_SALTED_PREFIX + salt + cipherText
  }
}
