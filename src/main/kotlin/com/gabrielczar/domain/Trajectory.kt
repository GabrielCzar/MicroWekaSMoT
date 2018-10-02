package com.gabrielczar.domain

import com.gabrielczar.domain.GPSPoint
import java.util.*

class Trajectory(
        var tid: Int = 0,
        var points : Vector<GPSPoint> = Vector(),
        private var SRID : Int = -1
)