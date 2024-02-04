package com.mikifus.padland.ActionModes

interface IAnyActionModeActive {
    var isAnyActionModeActive: Boolean
}

class AnyActionModeActive: IAnyActionModeActive {
    override var isAnyActionModeActive: Boolean = false
}