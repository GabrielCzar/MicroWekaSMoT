package com.gabrielczar.dao

import com.mchange.v2.c3p0.ComboPooledDataSource
import mu.KLogging
import java.sql.Connection
import java.sql.SQLException

object ConnectionPool : KLogging() {

    fun getConnection(): Connection? {
        try {
            val pool = ComboPooledDataSource()
            pool.driverClass = ConnectionProperties.driverClass
            pool.password = ConnectionProperties.pass
            pool.jdbcUrl = ConnectionProperties.host
            pool.user = ConnectionProperties.user

            pool.minPoolSize = 3
            pool.acquireIncrement = 5
            pool.maxPoolSize = 10
            pool.checkoutTimeout = 300
            pool.maxStatements = 50

            return pool.connection
        } catch (e: SQLException) {
            logger.error { e }
        }
        return null
    }

}