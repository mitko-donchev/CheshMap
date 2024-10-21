package com.epicmillennium.cheshmap.domain.usecase.watersource

import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository

class AddAllWaterSourcesUseCase(private val waterSourcesRepository: WaterSourcesRepository) {
    suspend operator fun invoke(waterSources: List<WaterSource>): Result<Boolean> = Result.runCatching {
        waterSourcesRepository.addAll(waterSources)
        true
    }
}