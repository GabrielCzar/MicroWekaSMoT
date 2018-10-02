package com.gabrielczar.domain

import java.util.*

/**
 * Set of the intersections between trajectory points and relevant features *
 */
class InterceptTrajectoryPointsAndRelevantFeatures (
        var intercepts: Vector<Intercept> = Vector()
) {

    fun isIn(pointGid : Int) : Intercept? {
        return intercepts.firstOrNull { it.pointGID == pointGid }
    }

}
