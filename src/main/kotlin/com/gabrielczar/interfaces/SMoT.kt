package com.gabrielczar.interfaces

import com.gabrielczar.domain.AssociatedParameter
import com.gabrielczar.domain.Stop
import com.gabrielczar.domain.Trajectory

interface SMoT {
    fun run (trajectory: Trajectory, associatedParameters: Array<AssociatedParameter>) : List<Stop>
}