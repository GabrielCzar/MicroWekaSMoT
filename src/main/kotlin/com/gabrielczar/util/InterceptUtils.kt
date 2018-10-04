package com.gabrielczar.util

import com.gabrielczar.domain.Intercept

fun isIn(intercepts: List<Intercept>, pointGid : Int) : Intercept? {
    return intercepts.firstOrNull { it.pointGID == pointGid }
}