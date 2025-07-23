package kz.market.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseProductService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun <T : Any> observeAllFrom(
        collectionName: String,
        type: Class<T>
    ): Flow<List<T>> = callbackFlow {
        val listener = firestore.collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(type)
                } ?: emptyList()

                trySend(documents)
            }
        awaitClose { listener.remove() }
    }

    suspend fun <T : Any> getById(
        collectionName: String,
        documentId: String,
        type: Class<T>
    ): Result<T?> = try {
        val snapshot = firestore.collection(collectionName)
            .document(documentId)
            .get()
            .await()
        Result.success(snapshot.toObject(type))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun <T : Any> putTo(
        collectionName: String,
        documentId: String,
        data: T
    ): Result<Unit> = try {
        firestore.collection(collectionName)
            .document(documentId)
            .set(data)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteFrom(
        collectionName: String,
        documentId: String
    ): Result<Unit> = try {
        firestore.collection(collectionName)
            .document(documentId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}