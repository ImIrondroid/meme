package com.kakao.iron.di

import com.kakao.iron.ui.camera.CameraViewModel
import com.kakao.iron.ui.storage.ManageViewModel
import com.kakao.iron.ui.detail.SaveViewModel
import com.kakao.iron.ui.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SaveViewModel(get(), get())
    }

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        ManageViewModel(get(), get())
    }

    viewModel {
        CameraViewModel(get(), get())
    }
}