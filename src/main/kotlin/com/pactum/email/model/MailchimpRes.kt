package com.pactum.email.model

data class MailchimpRes(
  val type: String,
  val title: String,
  val status: Int,
  val detail: String,
  val instance: String
)
