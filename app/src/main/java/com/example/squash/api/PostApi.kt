package com.example.squash.api


import android.database.Observable
import com.example.squash.api.posts.Post
import com.google.android.gms.tasks.Task
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*

interface PostApi {

    @GET("/api/recent/")
    fun getPost(@Query("opuuid") opuuid: String,
                @Query("number_of_posts") number_of_posts: Int?): Call<ListingResponse>

    @POST("/api/submit/")
    @FormUrlEncoded
    fun makePost(@Field("imageuuid") imageuuid: String?,
                         @Field("reply_to") reply_to: Long?,
                         @Field("opuuid") opuuid: String,
                         @Field("contents") contents: String): Call<PostResponse>


    @POST("/api/vote/")
    @FormUrlEncoded
    fun makeDescision(@Field("opuuid") opuuid: String,
                      @Field("post_number") post_number: Long,
                      @Field("descision") descision: Boolean?): Call<PostResponse>

    @GET("/api/post/")
    fun getSinglePost(@Query("post_number") post_number: Long,
                      @Query("opuuid") opuuid: String): Call<ListingResponse>

    @GET("/api/replies/")
    fun getComments(@Query("post_number") post_number: Long,
                    @Query("opuuid") opuuid: String): Call<ListingResponse>

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
            .host("ec2-3-15-217-5.us-east-2.compute.amazonaws.com")
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