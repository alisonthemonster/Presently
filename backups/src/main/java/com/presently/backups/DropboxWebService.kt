package com.presently.backups

import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Used to make requests to the Dropbox API. Authentication headers are automatically
 * added to each request with up to date access tokens. See AccessTokenAuthenticator.
 */
interface DropboxWebService {

    @POST("files/upload")
    suspend fun uploadFile(
        @Body body: RequestBody,
        @Header("Dropbox-API-Arg") args: String,
    )
}
