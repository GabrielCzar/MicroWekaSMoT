package com.gabrielczar.domain

class Trajectory(
        var tid: Int = 0,
        var points : List<GPSPoint> = emptyList(),
        private var SRID : Int = 4326
)