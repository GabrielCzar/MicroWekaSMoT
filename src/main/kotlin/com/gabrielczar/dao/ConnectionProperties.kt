package com.gabrielczar.dao

import com.gabrielczar.util.*
import java.io.FileInputStream
import java.util.*

object ConnectionProperties {
    private val properties : Properties = getProperties()

    var driverClass: String = properties.getProperty(DATABASE_DRIVER)
    var user: String = properties.getProperty(DATABASE_USER)
    var pass: String = properties.getProperty(DATABASE_PASS)
    var host: String = properties.getProperty(DATABASE_HOST)

    private fun getProperties() : Properties {
        val configPath = "${System.getProperty("user.dir")}/$APPLICATION_RESOURCES/$APPLICATION_PROPERTIES"
        val properties = Properties()
        properties.load(FileInputStream(configPath))
        return properties
    }
}
