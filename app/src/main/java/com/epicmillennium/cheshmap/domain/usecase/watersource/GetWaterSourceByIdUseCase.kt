package com.epicmillennium.cheshmap.domain.usecase.watersource

import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository
import kotlinx.coroutines.flow.Flow

class GetWaterSourceByIdUseCase(private val waterSourcesRepository: WaterSourcesRepository) {
    suspend operator fun invoke(id: String): Result<Flow<WaterSource?>> =
        Result.runCatching { waterSourcesRepository.getWaterSourceById(id) }
}