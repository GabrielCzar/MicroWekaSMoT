package com.gabrielczar

import com.vividsolutions.jts.geom.Point
import java.sql.Timestamp

data class GPSPoint (
        var time: Timestamp,
        var point: Point,

        var tid: Int = 0,
        var gid: Int = 0,
        var cluster: Cluster? = null,
        var clusterId: Int = 0,
        var speed: Double = 0.0,
        var amenity: String? = null,

        private var timeIndex: Int = 0
)
