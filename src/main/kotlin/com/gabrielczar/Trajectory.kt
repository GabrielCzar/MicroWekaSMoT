package com.gabrielczar

import java.util.*

class Trajectory(
    var tid: Int = 0,
    var points : Vector<GPSPoint> = Vector(),
    private var SRID : Int = -1
)