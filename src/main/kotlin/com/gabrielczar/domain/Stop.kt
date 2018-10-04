package com.gabrielczar.domain

import java.util.*

/**
 *
 * @param tid
 * @param pk
 * @param enterTime initial time
 * @param leaveTime ending time
 * @param gid serial number
 * @param minTime Minimum time in milliseconds
 * @param tableName Table name
 * @param amenity interest point type
 * @param points lasts stops
 * @param pts
 * @param SRID default is -1 based in postgres database
 * @param isBuffer is or not a buffer (maybe, his inside the radius is better definition)
 *
 * */
data class Stop (
        var tid: Int = 0,
        var pk: Int = 0,
        var enterTimes: Long = 0,
        var leaveTimes: Long = 0,
        var gid: Int = 0,
        var minTime: Int = 0,
        var tableName: String? = null,
        var amenity: String? = null,
        var points: Vector<GPSPoint> = Vector(),
        var pts: Vector<GPSPoint> = Vector(),
        var SRID: Int = 4326,
        var isBuffer: Boolean = false
) {

    // respecting the minimum stop points
    fun check(): Boolean = pts.size >= 2 && ((pts.lastElement().time - enterTimes) >= (minTime * 1000))

    fun addPoint(pt: GPSPoint, rf: String, minTime: Int, gid: Int) {
        this.tid = pt.tid
        this.enterTimes = pt.time
        this.leaveTimes = -1
        this.gid = gid
        this.tableName = rf
        this.minTime = minTime
        this.pts.addElement(pt)
    }

    fun addPoint(pt: GPSPoint) {
        this.pts.addElement(pt)
    }
}