package com.epicmillennium.cheshmap.domain.usecase.watersource

import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository
import kotlinx.coroutines.flow.Flow

class GetAllWaterSourcesUseCase(private val waterSourcesRepository: WaterSourcesRepository) {
    suspend operator fun invoke(): Result<Flow<List<WaterSource>>> =
        Result.runCatching { waterSourcesRepository.getAll() }
}