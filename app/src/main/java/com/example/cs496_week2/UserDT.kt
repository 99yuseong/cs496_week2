package com.example.cs496_week2

import java.util.*
import kotlin.collections.ArrayList

data class UserDT (
    val id: Long,
    val name: String,
    val friend: ArrayList<Long>,
    val group: ArrayList<Long>,
    val running: ArrayList<RunningDT>
)

data class RunningDT (
    val date: Date,
    val coordinate: ArrayList<Pair<Double, Double>>
)