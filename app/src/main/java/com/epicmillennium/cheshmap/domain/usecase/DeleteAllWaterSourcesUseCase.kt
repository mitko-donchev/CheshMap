package com.epicmillennium.cheshmap.domain.usecase

import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository

class DeleteAllWaterSourcesUseCase(private val waterSourcesRepository: WaterSourcesRepository) {
    suspend operator fun invoke(): Result<Boolean> = Result.runCatching {
        waterSourcesRepository.deleteAll()
        true
    }
}