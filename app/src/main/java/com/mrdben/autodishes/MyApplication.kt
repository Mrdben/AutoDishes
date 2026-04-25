package com.mrdben.autodishes

import android.app.Application
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mrdben.autodishes.data.AppDatabase
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.familygraphs.viewmodel.FamilyViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.mrdben.autodishes.dishgraphs.viewmodel.DishViewModel
import com.mrdben.autodishes.ordergraphs.viewmodel.MakeMealViewModel
import com.mrdben.autodishes.ordergraphs.viewmodel.OrderViewModel

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val familyRepository by lazy {
        FamilyRepository(database.familyDao(), database.elderDao())
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                FamilyViewModel(application.familyRepository)
            }
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                OrderViewModel(application.familyRepository)
            }
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                MakeMealViewModel(application.familyRepository)
            }
            initializer {
                DishViewModel()
            }
        }
    }
}