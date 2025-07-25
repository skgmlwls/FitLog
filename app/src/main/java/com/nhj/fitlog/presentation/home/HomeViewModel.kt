package com.nhj.fitlog.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nhj.fitlog.FitLogApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    val application = context as FitLogApplication

}