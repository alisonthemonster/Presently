package com.presently.backups

import com.google.common.truth.Truth.assertThat
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.fail
import org.junit.Test


class AccessTokenAuthenticatorTest {

    @Test
    fun `GIVEN an AccessTokenAuthenticator WHEN authenticate is called AND there are no existing tokens THEN return null`() {
        val credentialProvider = object : DropboxCredentialProvider {
            override fun getAccessToken(): String? {
                return null
            }

            override fun refreshTokens(): String? {
                fail("Refresh should not be called")
                return null
            }
        }

        val authenticator = AccessTokenAuthenticator(credentialProvider)
        val oldToken = "oldToken"
        val dummyRequest: Request = Request.Builder()
            .url("http://www.google.com")
            .addHeader("Authorization", "Bearer $oldToken")
            .get()
            .build()
        val response: Response = Response.Builder()
            .request(dummyRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        val actual = authenticator.authenticate(null, response)

        assertThat(actual).isNull()
    }

    @Test
    fun `GIVEN an AccessTokenAuthenticator WHEN authenticate is called THEN return refreshed token`() {
        val credentialProvider = object : DropboxCredentialProvider {
            override fun getAccessToken(): String? {
                return "firstAccessToken"
            }

            override fun refreshTokens(): String? {
                return "newToken"
            }
        }
        val expected = Request.Builder()
            .url("http://www.google.com")
            .addHeader("Authorization", "Bearer newToken")
            .get()
            .build()

        val authenticator = AccessTokenAuthenticator(credentialProvider)
        val dummyRequest: Request = Request.Builder()
            .url("http://www.google.com")
            .addHeader("Authorization", "Bearer firstAccessToken")
            .get()
            .build()
        val response: Response = Response.Builder()
            .request(dummyRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        val actual = authenticator.authenticate(null, response)

        assertThat(actual.toString()).isEqualTo(expected.toString())
    }

    @Test
    fun `GIVEN an AccessTokenAuthenticator WHEN authenticate is called THEN return new token`() {
        val credentialProvider = object : DropboxCredentialProvider {
            var timesGetAccessTokenIsCalled = 0
            override fun getAccessToken(): String? {
                timesGetAccessTokenIsCalled++
                return if (timesGetAccessTokenIsCalled == 1) "firstAccessToken" else "secondAccessToken"
            }

            override fun refreshTokens(): String? {
                return "newToken"
            }
        }
        val expected = Request.Builder()
            .url("http://www.google.com")
            .addHeader("Authorization", "Bearer secondAccessToken")
            .get()
            .build()

        val authenticator = AccessTokenAuthenticator(credentialProvider)
        val dummyRequest: Request = Request.Builder()
            .url("http://www.google.com")
            .addHeader("Authorization", "Bearer firstAccessToken")
            .get()
            .build()
        val response: Response = Response.Builder()
            .request(dummyRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        val actual = authenticator.authenticate(null, response)

        assertThat(actual.toString()).isEqualTo(expected.toString())
    }
}