package com.gabrielczar.handlers

import com.gabrielczar.dao.ConnectionPool
import com.gabrielczar.domain.*
import com.gabrielczar.interfaces.SMoT
import com.vividsolutions.jts.geom.Point
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

/**
 * Intersection-Based Stops and Moves of Trajectories
 * */
class IBSMoT (private val configuration: Configuration) : SMoT  {
    private val conn : Connection = ConnectionPool.getConnection() ?: throw SQLException("Connection failed")
    private val stmt = conn.createStatement()

    private fun searchByActiveStops(trajectoryId : Int, associatedParameter: AssociatedParameter) : List<Stop> {
        val sql = "SELECT * FROM (SELECT ${configuration.tableIdName} as tid, ${configuration.tableTimestampName} as time, " +
                "${configuration.tableGeomName} as the_geom, ${configuration.tableSerialName} as serial_time " +
                "FROM ${configuration.tableName} WHERE ${configuration.tableIdName}=$trajectoryId ORDER BY " +
                "${configuration.tableTimestampName}) T JOIN " +
                "(SELECT ${associatedParameter.name} as table_name, A.gid, the_geom as rf_the_geom, buff_env, buf FROM ${associatedParameter.name} A JOIN " +
                "${associatedParameter.name}_envelope B ON (A.gid = B.gid) ) R ON (ST_Intersects(buff_env, T.the_geom) " +
                " AND ST_Intersects(buf,T.the_geom)) ORDER BY time"

        LOGGER.info("Search in db...")

        val initialTime = System.currentTimeMillis()

        val resultSet = stmt.executeQuery(sql)

        val endingTime  = System.currentTimeMillis()

        LOGGER.info("Search by relevant features from the ${associatedParameter.name} in trajectory $trajectoryId takes ${endingTime.minus(initialTime)} ms")

        val stops = mutableListOf<Stop>()
        val activeStops = ActiveStop()

        val serialTime = if (resultSet.next()) resultSet.getInt("serial_time") else 0

        while (resultSet.next()) {
            val tid           = resultSet.getInt("tid")
            val gid           = resultSet.getInt("gid")
            val time  : Long  = resultSet.getTimestamp("time").time
            val point : Point = resultSet.getObject("the_geom") as Point
            val serialTimeAux = resultSet.getInt("serial_time")

            val pt = GPSPoint(tid = tid, gid = gid, time = time, point = point)

            stops.addAll(activeStops.beginTime())

            if (serialTimeAux - serialTime > 1)
                stops.addAll(activeStops.beginTime())

            val auxGid = resultSet.getObject("gid")

            if (auxGid != null) {
                val value : Int = associatedParameter.value ?: 0
                activeStops.addPoint(pt, value, associatedParameter.name)
            }
        }

        resultSet.close()

        //Forces the stops pending to close
        stops.addAll(activeStops.beginTime())
        stops.addAll(activeStops.beginTime())

        LOGGER.info("Created stops")
        LOGGER.info("Check stops")

        // Check mean time of each stop
        return stops.filter { (it.leaveTimes - it.enterTimes) < (it.minTime * 1000) }.also {
            LOGGER.info("Removed invalid stops")
        }
    }

    /**
     * Finds the stops in a trajectory.
     *
     * @param trajectory        The trajectory being analyzed.
     * @param associatedParameters  The Associated Parameters array used.
     *
     * @throws SQLException     If any table didn't exist in the BD, or a field.
     */
    @Throws(SQLException::class)
    override fun run(trajectory: Trajectory, associatedParameters: Array<AssociatedParameter>) : List<Stop> {
        return associatedParameters.map { searchByActiveStops(trajectory.tid, it) }.reduce { acc, list -> acc.plus(list) }.also {
            stmt.close()
        }
    }

    companion object {
        val LOGGER : Logger = LoggerFactory.getLogger(IBSMoT::class.java)
    }
}