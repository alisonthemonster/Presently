package com.presently.backups.wiring

import com.presently.backups.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object DropboxModule {

    @Provides
    fun provideDropboxCredentialProvider(provider: RealDropboxCredentialProvider): DropboxCredentialProvider {
        return provider
    }

    @Provides
    fun provideOkhttpClient(dropboxCredentialProvider: DropboxCredentialProvider): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(AccessTokenAuthenticator(dropboxCredentialProvider))
            .addInterceptor(AccessTokenInterceptor(dropboxCredentialProvider))
            .build()
    }

    @Provides
    fun provideDropboxWebService(okhttpClient: OkHttpClient): DropboxWebService {
        val retrofit = Retrofit.Builder()
            .client(okhttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://content.dropboxapi.com/2/")
            .build()
        return retrofit.create(DropboxWebService::class.java)
    }

    @Provides
    fun provideDropboxAuthService(): DropboxAuthService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.dropbox.com/")
            .build()
        return retrofit.create(DropboxAuthService::class.java)
    }
}