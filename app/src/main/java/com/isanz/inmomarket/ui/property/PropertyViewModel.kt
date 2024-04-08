import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.isanz.inmomarket.utils.entities.Chat
import com.isanz.inmomarket.utils.entities.Property
import kotlinx.coroutines.tasks.await


class PropertyViewModel {

    private val db = FirebaseFirestore.getInstance()

    private val database: FirebaseDatabase = Firebase.database


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

    fun addChat(senderId: String, recipientId: String, callback: (String) -> Unit) {
        val chatRef = database.getReference("chats")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var chatId: String? = null
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat?.membersId?.containsAll(listOf(senderId, recipientId)) == true ||
                        chat?.membersId?.containsAll(listOf(recipientId, senderId)) == true) {
                        chatId = snapshot.key
                        break
                    }
                }
                if (chatId == null) {
                    chatId = chatRef.push().key!!
                    chatRef.child(chatId).setValue(Chat(chatId = chatId, membersId = mutableListOf(senderId, recipientId)))
                }
                callback(chatId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ChatViewModel", "Error adding chat", databaseError.toException())
            }
        })
    }

}