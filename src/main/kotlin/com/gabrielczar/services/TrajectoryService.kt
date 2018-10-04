package com.gabrielczar.services

import com.gabrielczar.dao.ConnectionPool
import com.gabrielczar.domain.Stop
import com.gabrielczar.util.averageSpeed
import java.sql.Statement
import java.util.*

fun saveStopsAndMoves(tableStopName : String,
                      stops: Vector<Stop>,
                      buffer: Double = 50.0) {
    var stopId = 0

    for (stop in stops) {
        try {
            val s: Statement? = ConnectionPool.getConnection()?.createStatement()

            val stopName = "stop__${stop.gid}__${stop.amenity}"
            val sql = "INSERT INTO $tableStopName (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg) " +
                    "VALUES (${stop.tid}, $stopId, ${stop.enterTimes}, ${stop.leaveTimes}', ${stop.gid}, $stopName, " +
                    "${stopToSql(stop, buffer)}, ${stop.tableName},${averageSpeed(stop)}"
            stopId++

            s?.execute(sql)

        } catch (e: Exception) {
            // log error
        }
    }
}

fun stopToSql(stop: Stop, buffer: Double): String {
    // respecting the minimum of 4 points
    if (stop.pts.size < 2)
        return "null"

    var sql = "ST_LineFromText('LINESTRING("

    for (pt in stop.pts) sql += "${pt.point.x}_${pt.point.y},"

    sql = "${sql.substring(0, sql.length - 2)}), ${stop.SRID})"


    if (stop.isBuffer) {
        sql = "ST_Multi(ST_Buffer($sql::geography,$buffer)::geometry)"
    }

    return sql
}


