package com.engfred.callguardian.domain.usecases

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit

class IsFirstLaunchUseCase @Inject constructor(
    private val preferences: SharedPreferences
) {
    operator fun invoke(): Boolean {
        return preferences.getBoolean("first_launch", true)
    }

    fun markAsNotFirst() {
        preferences.edit { putBoolean("first_launch", false) }
    }
}