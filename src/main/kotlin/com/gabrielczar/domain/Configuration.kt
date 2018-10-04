package com.gabrielczar.domain

data class Configuration (
    val tableIdName : String = "taxiId",
    val tableTimestampName : String = "date_time",
    val tableGeomName : String = "geometry",
    val tableSerialName : String = "serial",
    val tableName : String = "taxi_data"
)