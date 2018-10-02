package com.gabrielczar.domain

import java.sql.Date

data class TrajectoryData(val tid : Int, val time: Date, val latitude: Double, val longitude: Double)