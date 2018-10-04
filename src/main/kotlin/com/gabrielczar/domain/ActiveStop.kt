package com.gabrielczar.domain

import java.util.*

class ActiveStop {
    private val stops: Hashtable<Int, StopMap> = Hashtable()

    // check if it executed more that one time, maybe this execute redundant actions
    fun beginTime(): Vector<Stop> {
        val closedStops = Vector<Stop>()

        // close not continuous stops in the last timestamp
        val stopKeys = stops.keys()

        while (stopKeys.hasMoreElements()) {
            val key = stopKeys.nextElement()

            stops[key]?.run {
                if (!added) {
                    stop?.let {
                        it.leaveTimes = it.pts.lastElement().time
                        closedStops.addElement(it)
                    }
                    stops.remove(key)
                } else {
                    added = false
                }
            }
        }

        return closedStops
    }

    /***
     * Add stop point
     *
     * @param point   is the point candidate to stop
     * @param minTime is the intersection minimum time
     */
    fun addPoint(point: GPSPoint, minTime: Int, tableName: String) {
        // stores the gid of the rf that intercepts the point
        val key = point.gid

        if (stops.containsKey(key)) {
            stops[key]?.let {
                it.stop?.pts?.addElement(point)
                it.added = true
            }
        } else {
            Stop(
                    tid = point.tid,
                    enterTimes = point.time,
                    gid = point.gid,
                    tableName = tableName,
                    minTime = minTime
            ).let {
                it.pts.addElement(point)
                stops[key] = StopMap(stop = it, added = true)
            }

        }
    }

    internal inner class StopMap(
            var stop: Stop? = null,
            var added : Boolean = false
    )
}

