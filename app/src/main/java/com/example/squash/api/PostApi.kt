package com.example.squash.api


import android.database.Observable
import com.example.squash.api.posts.Post
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface PostApi {

    @GET("/api/recent/100")
    fun getPost(): Call<ListingResponse>

    @POST("/api/submit/")
    @FormUrlEncoded
    fun makePost(@Field("imageuuid") imageuuid: String?,
                         @Field("reply_to") reply_to: Long?,
                         @Field("opuuid") opuuid: String,
                         @Field("contents") contents: String): Call<PostResponse>

    class PostResponse(val results: String)

    class ListingResponse(val results: List<Post>)


    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder()
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        //private const val BASE_URL = "https://www.reddit.com/"
        var httpurl = HttpUrl.Builder()
            .scheme("http")
            .host("ec2-3-133-82-128.us-east-2.compute.amazonaws.com")
            .port(5000)
            .build()
        fun create(): PostApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): PostApi {

            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(buildGsonConverterFactory())
                .build()
                .create(PostApi::class.java)
        }
    }

}