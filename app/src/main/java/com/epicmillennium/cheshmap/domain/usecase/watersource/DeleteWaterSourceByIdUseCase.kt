package com.epicmillennium.cheshmap.domain.usecase.watersource

import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository

class DeleteWaterSourceByIdUseCase(private val waterSourcesRepository: WaterSourcesRepository) {
    suspend operator fun invoke(id: String): Result<Boolean> = Result.runCatching {
        waterSourcesRepository.deleteWaterSourceById(id)
        true
    }
}