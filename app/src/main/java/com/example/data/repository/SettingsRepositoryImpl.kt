package com.example.data.repository

import com.example.data.local.dao.SettingsDao
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Settings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class SettingsRepositoryImpl(private val settingsDao: SettingsDao) : SettingsRepository {
    override fun getSettings(): Flow<Settings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toDomain() ?: Settings()
        }
    }

    override suspend fun updateSettings(settings: Settings) {
        settingsDao.insertSettings(settings.toEntity())
    }
}
