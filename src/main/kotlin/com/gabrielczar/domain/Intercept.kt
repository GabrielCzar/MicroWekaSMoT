package com.gabrielczar.domain

data class Intercept (
        var pointGID: Int = 0,
        var relevantFeatureGID: Int = 0,
        var relevantFeatureName: String = "",
        var minTime: Int = 0,
        var amenity: String = ""
)
