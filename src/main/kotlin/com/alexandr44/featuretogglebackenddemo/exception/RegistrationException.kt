package com.alexandr44.featuretogglebackenddemo.exception

import java.io.Serial

class RegistrationException(message: String) : RuntimeException(message) {

    companion object {
        @Serial
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 5038013920011159396L
    }

}
