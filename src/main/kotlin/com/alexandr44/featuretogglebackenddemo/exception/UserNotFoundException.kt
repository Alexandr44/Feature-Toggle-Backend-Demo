package com.alexandr44.featuretogglebackenddemo.exception

import java.io.Serial

class UserNotFoundException(message: String) : RuntimeException(message) {

    companion object {
        @Serial
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 5038013920011159377L
    }

}
