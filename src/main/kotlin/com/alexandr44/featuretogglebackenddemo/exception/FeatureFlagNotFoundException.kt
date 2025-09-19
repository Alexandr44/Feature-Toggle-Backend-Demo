package com.alexandr44.featuretogglebackenddemo.exception

import java.io.Serial

class FeatureFlagNotFoundException(message: String) : RuntimeException(message) {

    companion object {
        @Serial
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 5038013929911159377L
    }

}
