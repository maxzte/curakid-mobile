package com.misterio.core.network.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class BaseApiTest<T : Any> {

    protected lateinit var mockWebServer: MockWebServer
    protected lateinit var api: T

    abstract fun createApi(retrofit: Retrofit): T

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = createApi(retrofit)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    protected fun enqueueResponse(body: String, code: Int = 200) {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(body)
                .setResponseCode(code)
        )
    }
}
