package com.gabrielczar

import com.vividsolutions.jts.geom.Point
import java.sql.Timestamp
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
        var enterTime: Timestamp? = null,
        var leaveTime: Timestamp? = null,
        var gid: Int = 0,
        var minTime: Int = 0,
        var tableName: String? = null,
        var amenity: String? = null,
        var points: Vector<GPSPoint> = Vector(),
        var pts: Vector<GPSPoint> = Vector(),
        var SRID: Int = 4326,
        var isBuffer: Boolean = false
) {
    fun check(): Boolean {
        if (pts.size >= 2) { // respecting the minimum stop points
            val leaveTime : Long = pts.lastElement().time.time

            enterTime?.time?.let { time ->
                if (leaveTime - time >= (minTime * 1000)) return true
            }
        }
        return false
    }

    fun addPoint(pt: GPSPoint, rf: String, minTime: Int, gid: Int) {
        this.tid = pt.tid
        this.enterTime = pt.time

        this.leaveTime = null
        this.gid = gid
        this.tableName = rf
        this.minTime = minTime

        this.pts.addElement(pt)
    }

    fun addPoint(pt: GPSPoint) {
        this.pts.addElement(pt)
    }
}