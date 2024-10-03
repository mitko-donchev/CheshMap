package com.epicmillennium.cheshmap.utils.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.epicmillennium.cheshmap.domain.marker.FirestoreWaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.usecase.AddAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_WATER_SOURCES
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class WaterSourcesRetrieverWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseFirestore: FirebaseFirestore,
    private val addAllWaterSourcesUseCase: AddAllWaterSourcesUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch data from Firestore synchronously (await)
            val data = firebaseFirestore.collection(FIRESTORE_COLLECTION_WATER_SOURCES)
                .get()
                .await() // Use await to suspend and wait for the result

            // Map Firestore data to your WaterSource domain model
            val waterSources = data.documents.mapNotNull { documentSnapshot ->
                // Convert Firestore document to FirestoreWaterSource and include the document ID
                val firestoreWaterSource = documentSnapshot.toObject(FirestoreWaterSource::class.java)
                firestoreWaterSource?.copy(id = documentSnapshot.id) // Use documentSnapshot.id as the Firestore ID
            }.map { firestoreWaterSource ->
                // Convert FirestoreWaterSource to your domain model WaterSource
                WaterSource.fromFirestoreWaterSource(firestoreWaterSource)
            }

            // Process the water sources if not empty
            if (waterSources.isNotEmpty()) {
                addAllWaterSourcesUseCase.invoke(waterSources)
                    .onSuccess {
                        return Result.success()
                    }
                    .onFailure { exception ->
                        Log.e(
                            "WaterSourcesRetrieverWorker",
                            "Error fetching latest water sources from firebase: ",
                            exception
                        )
                        return Result.retry()
                    }
            } else {
                Log.e("WaterSourcesRetrieverWorker", "No water sources found in Firestore")
                return Result.failure()
            }

            Result.failure()
        } catch (exception: Exception) {
            Log.e(
                "WaterSourcesRetrieverWorker",
                "Error fetching latest water sources from firebase: ",
                exception
            )
            return Result.retry()
        }
    }
}