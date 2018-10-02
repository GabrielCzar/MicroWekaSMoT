package com.gabrielczar

import com.gabrielczar.dao.ConnectionPool

fun main(args: Array<String>) {
    println(ConnectionPool.getConnection())
}

