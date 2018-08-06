package com.gabrielczar

import com.vividsolutions.jts.geom.Point
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.logging.Logger

class SMoT2 (private val conn : Connection) {
    private val LOGGER : Logger = Logger.getLogger("SMoT_2")

    @Throws(SQLException::class)
    fun run(trajectory: Trajectory, intercepts: InterceptTrajectoryPointsAndRelevantFeatures) {

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
        val activeStop : ActiveStop = ActiveStop()
        val stops : Vector<Stop> = Vector()
        var stop : Stop = Stop()

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
                    stop.addPoint(point, rfIntercept.relevantFeatureGID, rfIntercept.relevantFeatureName, rfIntercept.pointGID)
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
                        stop.addPoint(point, rfIntercept.relevantFeatureGID, rfIntercept.relevantFeatureName, rfIntercept.pointGID) // saves the enterTime
                        gidRelevantFeature = rfIntercept.pointGID
                    }
                }
            }

            rfIntercept ?: ap
            if (rfIntercept == null){
                if (!first) {
                    // I didn't have an Intercept

                    if (st.check()) {
                        stops.addElement(st)
                    }
                } else {
                    first = true
                }
            }

            // refresh value
            serialGid = gidActual
        }

        if (!first && stop.check()) {
            stops.addElement(stop) //if passes, it's added
        }

        // SAVE

        resultSet.close()
    }
}