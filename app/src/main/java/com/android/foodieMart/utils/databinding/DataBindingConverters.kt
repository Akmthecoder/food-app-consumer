package com.android.foodieMart.utils.databinding

import android.text.TextUtils
import androidx.databinding.InverseMethod

class DataBindingConverters {
    companion object {

        @InverseMethod("convertIntegerToString")
        @JvmStatic
        fun convertStringToInteger(value: String): Int? {
            if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)) {
                return null
            }
            return value.toIntOrNull()
        }

        @JvmStatic
        fun convertIntegerToString(value: Int?): String {
            return value?.toString() ?: ""
        }
    }
}