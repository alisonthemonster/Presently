package com.presently.backups

import com.google.common.truth.Truth.assertThat
import okhttp3.*
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class AccessTokenInterceptorTest {

    @Test
    fun `GIVEN an AccessTokenInterceptor WHEN intercept is called THEN return the modified response`() {
        var proceedWasCalled = false
        var getAccessTokenCalled = false
        var requestWasCalled = false
        var proceedWasCalledWith: Request? = null

        val credentialProvider = object : DropboxCredentialProvider {
            override fun getAccessToken(): String? {
                getAccessTokenCalled = true
                return "the-access-token"
            }

            override fun refreshTokens(): String? {
                Assert.fail("Refresh should not be called")
                return null
            }
        }
        val chain = object : Interceptor.Chain {
            override fun call(): Call {
                TODO("Not yet implemented")
            }

            override fun connectTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

            override fun connection(): Connection? {
                TODO("Not yet implemented")
            }

            override fun proceed(request: Request): Response {
                proceedWasCalled = true
                proceedWasCalledWith = request
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .build()
            }

            override fun readTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

            override fun request(): Request {
                requestWasCalled = true
                return Request.Builder()
                    .url("http://www.google.com")
                    .get()
                    .build()
            }

            override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun writeTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

        }
        val expected = Request.Builder()
            .url("http://www.google.com")
            .addHeader("Authorization", "Bearer the-access-token")
            .get()
            .build()

        val interceptor = AccessTokenInterceptor(credentialProvider)

        interceptor.intercept(chain)

        assertThat(proceedWasCalled).isTrue()
        assertThat(requestWasCalled).isTrue()
        assertThat(getAccessTokenCalled).isTrue()
        assertThat(proceedWasCalledWith.toString()).isEqualTo(expected.toString())
    }

    @Test
    fun `GIVEN an AccessTokenInterceptor WHEN intercept is called AND there is no token THEN do not modifiy the response`() {
        val credentialProvider = object : DropboxCredentialProvider {
            override fun getAccessToken(): String? {
                return null
            }

            override fun refreshTokens(): String? {
                Assert.fail("Refresh should not be called")
                return null
            }
        }

        var proceedWasCalled = false
        var requestWasCalled = false
        var proceedWasCalledWith: Request? = null

        val chain = object : Interceptor.Chain {
            override fun call(): Call {
                TODO("Not yet implemented")
            }

            override fun connectTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

            override fun connection(): Connection? {
                TODO("Not yet implemented")
            }

            override fun proceed(request: Request): Response {
                proceedWasCalled = true
                proceedWasCalledWith = request
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .build()
            }

            override fun readTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

            override fun request(): Request {
                requestWasCalled = true
                return Request.Builder()
                    .url("http://www.google.com")
                    .get()
                    .build()
            }

            override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun writeTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

        }
        val expected = Request.Builder()
            .url("http://www.google.com")
            .get()
            .build()

        val interceptor = AccessTokenInterceptor(credentialProvider)

        interceptor.intercept(chain)

        assertThat(proceedWasCalled).isTrue()
        assertThat(requestWasCalled).isTrue()
        assertThat(proceedWasCalledWith.toString()).isEqualTo(expected.toString())
    }
}