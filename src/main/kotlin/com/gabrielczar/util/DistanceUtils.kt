package com.gabrielczar.util

import com.gabrielczar.domain.GPSPoint
import com.gabrielczar.domain.Stop
import com.vividsolutions.jts.geom.Point
import java.sql.Timestamp


fun stopAverageSpeed(stop: Stop): Double {
    val gpsPoint: GPSPoint = stop.pts.firstElement()

    val initialTime = gpsPoint.timestamp
    val endingTime = stop.pts.lastElement().timestamp

    val sum : Double = stop.points.map { it.point }.zipWithNext { a, b -> euclideanDistance(a, b) }.sum().coerceAtLeast(0.0)

    val time = intervalBetweenTimestamps(initialTime, endingTime)
    return sum.div(time.div(1000))
}

fun intervalBetweenTimestamps(initialTimestamp: Timestamp, finalTimestamp: Timestamp) =
    finalTimestamp.time.minus(initialTimestamp.time)

fun euclideanDistance(initialPoint: Point, finalPoint: Point): Double =
        Math.sqrt(Math.pow(finalPoint.x - initialPoint.x, 2.0) + Math.pow(finalPoint.y - initialPoint.y, 2.0))
