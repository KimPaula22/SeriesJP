package com.example.seriesjp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seriesjp.datastore.SessionPreferences

class AuthViewModelFactory(
    private val sessionPreferences: SessionPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(sessionPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}