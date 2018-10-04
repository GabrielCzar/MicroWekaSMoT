package com.gabrielczar.handlers

import com.gabrielczar.dao.ConnectionPool
import com.gabrielczar.domain.*
import com.gabrielczar.interfaces.SMoT
import com.gabrielczar.util.isIn
import com.vividsolutions.jts.geom.Point
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

/**
 * Clustering-Based Stops and Moves of Trajectories
 * */
class CBSMoT (private val minTime : Int = 60,
              private val maxSpeed : Double = 1.1,
              private val maxAvgSpeed : Double = 0.9) : SMoT {


    override fun run(trajectory: Trajectory, associatedParameters: Array<AssociatedParameter>): List<Stop> {
        TODO("MAKE DEFAULT ALL SMoT's")
    }

    private val conn : Connection? = ConnectionPool.getConnection()

    @Throws(SQLException::class)
    fun run(trajectory: Trajectory, intercepts: Vector<Intercept>) : List<Stop> {

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
                    time = resultSet.getTimestamp(tableTimestampName).time,
                    point = resultSet.getObject(tableGeomName) as Point
            )

            val gidActual = point.gid

            val rfIntercept : Intercept? = isIn(intercepts.toList(), point.gid)

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

}

