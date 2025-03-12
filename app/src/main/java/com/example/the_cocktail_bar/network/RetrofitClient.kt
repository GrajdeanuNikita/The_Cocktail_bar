package com.example.the_cocktail_bar.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.the_cocktail_bar.CocktailR

object RetrofitClient {
    //api principale per usarlo con tutte le altre 
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

    val instance: CocktailApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CocktailApiService::class.java)
    }
}

interface CocktailApiService {
    @GET("search.php")
    suspend fun searchCocktail(@Query("s") name: String): CocktailR

    @GET("random.php")
    suspend fun getRandomCocktail(): CocktailR
} 