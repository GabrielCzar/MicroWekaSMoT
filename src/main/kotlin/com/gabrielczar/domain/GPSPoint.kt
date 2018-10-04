package com.gabrielczar.domain

import com.gabrielczar.enums.Cluster
import com.vividsolutions.jts.geom.Point

data class GPSPoint (
        var time : Long = 0,
        var point: Point,

        var tid: Int = 0,
        var gid: Int = 0,
        var cluster: Cluster? = null,
        var clusterId: Int = 0,
        var speed: Double = 0.0,
        var amenity: String? = null,

        private var timeIndex: Int = 0
)
