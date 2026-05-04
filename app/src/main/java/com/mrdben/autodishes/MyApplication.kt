package com.mrdben.autodishes

import android.app.Application
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mrdben.autodishes.data.AppDatabase
import com.mrdben.autodishes.familygraphs.data.FamilyRepository
import com.mrdben.autodishes.familygraphs.viewmodel.FamilyViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.mrdben.autodishes.api.TokenManager
import com.mrdben.autodishes.dishgraphs.data.DishRepository
import com.mrdben.autodishes.dishgraphs.viewmodel.DishViewModel
import com.mrdben.autodishes.ordergraphs.data.OrderRepository
import com.mrdben.autodishes.ordergraphs.viewmodel.MakeMealViewModel
import com.mrdben.autodishes.ordergraphs.viewmodel.OrderViewModel
import com.mrdben.autodishes.authgraphs.viewmodel.AuthViewModel

class MyApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val familyRepository by lazy {
        FamilyRepository(database.familyDao(), database.elderDao())
    }
    val dishRepository by lazy {
        DishRepository(database.dishDao())
    }

    val orderRepository by lazy {
        OrderRepository(database.orderDao())
    }
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                FamilyViewModel(application.familyRepository)
            }
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                OrderViewModel(application.familyRepository,
                               application.dishRepository,
                               application.orderRepository)
            }
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                MakeMealViewModel(application.familyRepository,
                                  application.dishRepository,
                                  application.orderRepository)
            }
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                DishViewModel(application.dishRepository)
            }
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                AuthViewModel(application.familyRepository)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 在这里一次性传入 Application 的 Context
        TokenManager.init(this)
    }
}