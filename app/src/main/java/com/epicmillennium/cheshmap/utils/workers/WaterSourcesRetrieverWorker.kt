package com.epicmillennium.cheshmap.utils.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.epicmillennium.cheshmap.domain.marker.FirestoreWaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.usecase.AddAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.utils.Constants.FAVOURITE_SOURCES
import com.epicmillennium.cheshmap.utils.Constants.FIRESTORE_COLLECTION_WATER_SOURCES
import com.epicmillennium.cheshmap.utils.clearAppCacheFromAttachments
import com.epicmillennium.cheshmap.utils.preferences.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@HiltWorker
class WaterSourcesRetrieverWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val addAllWaterSourcesUseCase: AddAllWaterSourcesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Clear all attachments from app cache for optimisation
            Log.v(this.toString(), "Clearing app cache from all attachments")
            applicationContext.clearAppCacheFromAttachments()

            // Fetch data from Firestore synchronously (await)
            val data = firestore.collection(FIRESTORE_COLLECTION_WATER_SOURCES)
                .get()
                .await() // Use await to suspend and wait for the result

            // Map Firestore data to WaterSource domain model
            var waterSources = data.documents.mapNotNull { documentSnapshot ->
                // Convert Firestore document to FirestoreWaterSource and include the document ID
                val firestoreWaterSource =
                    documentSnapshot.toObject(FirestoreWaterSource::class.java)
                firestoreWaterSource?.copy(id = documentSnapshot.id) // Use documentSnapshot.id as the Firestore ID
            }.map { firestoreWaterSource ->
                // Convert FirestoreWaterSource to domain model WaterSource
                WaterSource.fromFirestoreWaterSource(firestoreWaterSource)
            }

            // Process the water sources if not empty
            if (waterSources.isNotEmpty()) {
                val favWaterSources = getFavouriteWaterSources()
                if (favWaterSources.isNotEmpty()) {
                    waterSources = waterSources.map {
                        it.copy(isFavourite = favWaterSources.contains(it.id))
                    }
                }

                addAllWaterSourcesUseCase.invoke(waterSources)
                    .onSuccess {
                        Log.d("WaterSourcesRetrieverWorker", "Water sources fetched successfully")
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

    private suspend fun getFavouriteWaterSources(): Set<String> =
        userPreferencesRepository.dataStore.data.map { it[FAVOURITE_SOURCES] ?: emptySet() }.first()
}