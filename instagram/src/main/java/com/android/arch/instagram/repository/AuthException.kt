package com.android.arch.instagram.repository

sealed class AuthException(msg: String?) : Exception(msg) {
    class NullResponseBodyApiException(msg: String?) : AuthException(msg)
    class FailedResponseApiException(msg: String?) : AuthException(msg)
    class FailedAuthException(msg: String?) : AuthException(msg)
    class OnCanceledAuthException : AuthException("Request canceled")
}