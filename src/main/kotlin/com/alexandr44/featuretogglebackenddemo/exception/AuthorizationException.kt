package com.alexandr44.featuretogglebackenddemo.exception

import java.io.Serial

class AuthorizationException(message: String) : RuntimeException(message) {

    companion object {
        @Serial
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 8341719022912111167L
    }

}
