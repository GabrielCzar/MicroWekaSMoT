package com.gabrielczar.handlers

import com.gabrielczar.dao.ConnectionPool
import com.gabrielczar.domain.*
import com.gabrielczar.interfaces.SMoT
import com.vividsolutions.jts.geom.Point
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*

/**
 * Clustering-Based Stops and Moves of Trajectories
 * */
class CBSMoT (private val minTime : Int = 60,
              private val maxSpeed : Double = 1.1,
              private val maxAvgSpeed : Double = 0.9) : SMoT {


    override fun run(trajectory: Trajectory, relevantFeatures: Array<AssociatedParameter>): List<Stop> {
        TODO("MAKE DEFAULT ALL SMoT's")
    }

    private val conn : Connection? = ConnectionPool.getConnection()

    @Throws(SQLException::class)
    fun run(trajectory: Trajectory, intercepts: InterceptTrajectoryPointsAndRelevantFeatures) : List<Stop> {

        if (conn == null)
            throw SQLException("Connection failed")

        val tableIdName = "taxiId"
        val tableTimestampName = "date_time"
        val tableGeomName = "geometry"
        val tableSerialName = "serial"
        val tableName = "taxi_data"

        val statement = conn.createStatement()

        val sql = "select $tableIdName, $tableSerialName, $tableTimestampName, $tableGeomName from $tableName" +
                " where $tableIdName=${trajectory.tid} order by time;"

        LOGGER.info("Applying method SMoT...\n$sql")

        val resultSet : ResultSet = statement.executeQuery(sql)
        val stops : Vector<Stop> = Vector()
        var stop = Stop()

        var gidRelevantFeature = -1
        var first = true
        var serialGid = -1

        while (resultSet.next()) {
            // creates the point in the BD

            val point = GPSPoint(
                    tid = resultSet.getInt(tableIdName),
                    gid = resultSet.getInt(tableSerialName),
                    time = resultSet.getTimestamp(tableTimestampName),
                    point = resultSet.getObject(tableGeomName) as Point
            )

            val gidActual = point.gid

            val rfIntercept : Intercept? = intercepts.isIn(point.gid)

            rfIntercept?.let {
                if (first) {
                    first = false
                    stop.amenity = rfIntercept.amenity
                    stop.addPoint(
                            pt = point,
                            minTime = rfIntercept.relevantFeatureGID,
                            rf = rfIntercept.relevantFeatureName,
                            gid = rfIntercept.pointGID
                    )
                    gidRelevantFeature = rfIntercept.pointGID
                } else {
                    if (rfIntercept.pointGID == gidRelevantFeature) {
                        if (gidActual - serialGid <= 1) {
                            stop.amenity = rfIntercept.amenity
                            stop.addPoint(point)
                        }
                    } else {
                        first = false
                        stop = Stop()
                        stop.amenity = rfIntercept.amenity
                        stop.addPoint(
                                pt = point,
                                rf = rfIntercept.relevantFeatureName,
                                minTime = rfIntercept.relevantFeatureGID,
                                gid = rfIntercept.pointGID
                        ) // saves the enterTime
                        gidRelevantFeature = rfIntercept.pointGID
                    }
                }
            }

            if (rfIntercept == null){
                if (!first) {
                    // I didn't have an Intercept
                    if (stop.check()) {
                        stops.addElement(stop)
                    }
                } else {
                    first = true
                }
            }

            // refresh value
            serialGid = gidActual
        }

        if (!first && stop.check()) {
            stops.addElement(stop)
        }

        resultSet.close()
        // SAVE


        return stops
    }

    //    saveStopsAndMoves(stops = stops, tableStopName = "SMoT_2_TABLE_STOP_NAME")
    fun saveStopsAndMoves(tableStopName : String,
                          stops: Vector<Stop>,
                          buffer: Double = 50.0) {
        var stopId = 0

        for (stop in stops) {
            try {
                val s: Statement? = conn?.createStatement()

                val stopName = "stop__${stop.gid}__${stop.amenity}"
                val sql = "INSERT INTO $tableStopName (tid,stopid,start_time,end_time,stop_gid,stop_name,the_geom,rf,avg) " +
                        "VALUES (${stop.tid}, $stopId, ${stop.enterTime}, ${stop.leaveTime}', ${stop.gid}, $stopName, " +
                        "${stopToSql(stop, buffer)}, ${stop.tableName},${stopAvgSpeed(stop)}"
                stopId++

                s?.execute(sql)

            } catch (e: Exception) {
                LOGGER.info(e.message)
                break
            }
        }
    }

    companion object {

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

        fun stopAvgSpeed(stop: Stop): Double {
            var pt: GPSPoint = stop.pts.elementAt(0)

            val initialTime = pt.time.time
            var sum = 0.0

            for (point in stop.points) {
                sum += euclideanDistance(pt.point, point.point)
                pt = point
            }

            val endingTime = stop.pts.lastElement().time.time
            val time = endingTime - initialTime
            return sum / (time / 1000)
        }

        private fun euclideanDistance(point1: Point, point2: Point): Double {
            var dist = Math.pow(point2.x - point1.x, 2.0) + Math.pow(point2.y - point1.y, 2.0)
            dist = Math.sqrt(dist)
            return dist
        }
    }

}

val LOGGER : Logger = LoggerFactory.getLogger(CBSMoT::class.java)