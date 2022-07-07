package com.example.cs496_week2

import java.io.Serializable

data class KakaoAccount(
    val id: Long?,
    val email: String?,
    val nickname: String?,
    val profileUrl: String?
) : Serializable