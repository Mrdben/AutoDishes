import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Define the data we send to your Ali Cloud function
data class MealRequest(
    val name: String,
    val diseases: String,
    val allergies: String,
    val preferences: String
)

// 2. Define what we expect back from Qwen-Max (via the Cloud Function)
data class MealResponse(
    val mealPlan: String
)

// 3. The API Interface
interface AliCloudService {
    @POST("https://fc-mp-334e6ddc-7a19-4d9e-ae02-03449157fb4e-custom.next.bspapp.com/recommendMeal") // REPLACE THIS with your actual Ali Cloud URL path
    suspend fun getMealPlan(@Body request: MealRequest): MealResponse
}

// 4. The Retrofit Object (The Engine)
object RetrofitClient {
    private const val BASE_URL = "https://api.next.bspapp.com/" // REPLACE THIS

    val instance: AliCloudService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AliCloudService::class.java)
    }
}