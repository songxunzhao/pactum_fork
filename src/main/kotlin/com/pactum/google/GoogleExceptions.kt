package com.pactum.google

import com.pactum.exception.ServerFaultException

// google drive exceptions
class GoogleDriveFetchException : ServerFaultException("Google drive failed to fetch")
class GoogleDriveTimeoutException : ServerFaultException("Google drive timed out")
class GoogleStorageUploadException : ServerFaultException("Google storage failed to upload")
class GoogleStorageDownloadException : ServerFaultException("Google drive failed to download")
