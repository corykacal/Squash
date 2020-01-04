package com.example.squash.api


import com.example.squash.api.posts.Post
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SquashApi {

    @GET("/api/user/")
    fun getUserData(@Query("opuuid") opuuid: String
    ): Call<UserDataReponse>

    @GET("/api/my_post/")
    fun getMyPosts(@Query("opuuid") opuuid: String,
                       @Query("number_of_posts") number_of_posts: Int?,
                       @Query("page_number") page_number: Int?
    ): Call<ListingResponse>

    @GET("/api/recent/")
    fun getRecentPosts(@Query("opuuid") opuuid: String,
                       @Query("number_of_posts") number_of_posts: Int?,
                       @Query("page_number") page_number: Int?,
                       @Query("subject") subject: String?
    ): Call<ListingResponse>

    @GET("/api/hot/")
    fun getHotPosts(@Query("opuuid") opuuid: String,
                    @Query("number_of_posts") number_of_posts: Int?,
                    @Query("page_number") page_number: Int?,
                    @Query("subject") subject: String?
    ): Call<ListingResponse>

    @POST("/api/submit/")
    @FormUrlEncoded
    fun makePost(@Field("imageuuid") imageuuid: String?,
                 @Field("reply_to") reply_to: Long?,
                 @Field("opuuid") opuuid: String,
                 @Field("contents") contents: String,
                 @Field("subject") subject: String?,
                 @Field("latitude") latitude: Double,
                 @Field("longitude") longitude: Double
    ): Call<PostResponse>


    @POST("/api/vote/")
    @FormUrlEncoded
    fun makeDescision(@Field("opuuid") opuuid: String,
                      @Field("post_number") post_number: Long,
                      @Field("descision") descision: Boolean?
    ): Call<PostResponse>

    @GET("/api/post/")
    fun getSinglePost(@Query("post_number") post_number: Long,
                      @Query("opuuid") opuuid: String
    ): Call<ListingResponse>

    @GET("/api/replies/")
    fun getComments(@Query("post_number") post_number: Long,
                    @Query("opuuid") opuuid: String
    ): Call<ListingResponse>

    class PostResponse(val results: String)

    class ListingResponse(val results: List<Post>)

    class UserDataReponse(val results: List<UserData>)


    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder()
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        //private const val BASE_URL = "https://www.reddit.com/"
        var httpurl = HttpUrl.Builder()
            .scheme("http")
            .host("ec2-18-218-80-162.us-east-2.compute.amazonaws.com")
            .port(5000)
            .build()
        fun create(): SquashApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): SquashApi {

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
                .create(SquashApi::class.java)
        }
    }

}