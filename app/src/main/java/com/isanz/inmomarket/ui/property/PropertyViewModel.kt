import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.tasks.await

class PropertyViewModel {

    private val db = FirebaseFirestore.getInstance()

    suspend fun retrieveProperty(propertyId: String): Property? {
        return try {
            val document = db.collection("properties").document(propertyId).get().await()
            if (document.exists()) {
                document.toObject(Property::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("PropertyViewModel", "Error retrieving property", e)
            null
        }
    }
}