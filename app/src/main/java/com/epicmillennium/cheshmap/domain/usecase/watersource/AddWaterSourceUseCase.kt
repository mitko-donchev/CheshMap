package com.epicmillennium.cheshmap.domain.usecase.watersource

import com.epicmillennium.cheshmap.domain.marker.WaterSource
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository

class AddWaterSourceUseCase(private val waterSourcesRepository: WaterSourcesRepository) {
    suspend operator fun invoke(waterSource: WaterSource): Result<Boolean> = Result.runCatching {
        waterSourcesRepository.addWaterSource(waterSource)
        true
    }
}