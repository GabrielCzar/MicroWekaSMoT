package com.gabrielczar

import com.vividsolutions.jts.geom.Point
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.sql.Timestamp
import java.util.*

class IBSMoT (private val conn: Connection) : StopMoveAlgorithm {

    // CHECK IF IT IS THE METHOD TO BE USED OR NOT
    /**
     * SMOT1
     * Finds the stops in a trajectory.
     *
     * @param trajectory        The trajectory being analyzed.
     * @param relevantFeatures  The RelevantFeatures (AssociatedParameter) array used.
     * @throws SQLException     If any table didn't exist in the BD, or a field.
     *
     */
    @Throws(SQLException::class)
    override fun run(trajectory: Trajectory, relevantFeatures: Array<AssociatedParameter>) {

        val tableIdName = "taxiId"
        val tableTimestampName = "date_time"
        val tableGeomName = "geometry"
        val tableSerialName = "serial"
        val tableName = "taxi_data"

        val s : Statement = conn.createStatement()

        for (rf in relevantFeatures) {

            val sql = StringBuffer("SELECT * FROM (SELECT $tableIdName as tid, $tableTimestampName as time, $tableGeomName as the_geom, $tableSerialName as serial_time " +
                    "FROM $tableName WHERE $tableIdName=${trajectory.tid} ORDER BY $tableTimestampName) T JOIN " +
                    "(SELECT ${rf.name} as table_name, A.gid, the_geom as rf_the_geom, buff_env, buf FROM ${rf.name} A JOIN " +
                    "${rf.name}_envelope B ON (A.gid = B.gid) ) R ON (ST_Intersects(buff_env, T.the_geom) " +
                    " AND ST_Intersects(buf,T.the_geom)) ORDER BY time")

            println(sql.toString())

            val ini = Date()
            val rs = s.executeQuery(sql.toString()) // Execute Query
            val fim = Date()

            val intervalTime = java.util.Date(fim.time - ini.time)

            println("Main Query (RF: $rf): ${intervalTime.time} ms")

            val stops = Vector<Stop>()
            val activeStops = ActiveStops()
            var serialTime = 0
            var first = true

            while (rs.next()) {
                if (first) {
                    serialTime = rs.getInt("serial_time")
                    first = false
                }

                val tid = rs.getInt("tid")
                val gid = rs.getInt("gid")
                val time : Timestamp = rs.getTimestamp("time")
                val point : Point = rs.getObject("the_geom") as Point
                val serialTimeAux = rs.getInt("serial_time")

                val pt = GPSPoint(tid = tid, gid = gid, time = time, point = point)

                if (time.time != pt.time.time) {

                    stops.addAll(activeStops.beginTime())

                    if (serialTimeAux - serialTime > 1)
                        stops.addAll(activeStops.beginTime())
                }

                if (rs.getObject("gid") != null) {
                    activeStops.addPoint(pt, rf.value!!, rf.name)
                }

            }

            //Forces the stops pending to close

            stops.addAll(activeStops.beginTime())
            stops.addAll(activeStops.beginTime())

            //Tests mean time of each stop
            for (i in stops.indices.reversed()) {
                val st = stops.elementAt(i) as Stop

                if (st.leaveTime!!.time - st.enterTime!!.time < st.minTime * 1000) {
                    stops.removeElementAt(i)
                }
            }

            // save or return stops

            rs.close()
            s.close()
        }
    }
}