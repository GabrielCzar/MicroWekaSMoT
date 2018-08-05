package com.gabrielczar

interface StopMoveAlgorithm {
    fun run (trajectory: Trajectory, relevantFeatures: Array<AssociatedParameter>)
}