package com.mikifus.padland.Utils.Export

/**
 * Record that can be matched against existing ones when importing.
 */
interface IImportMatchable {
    /**
     * Copy with the fields ignored on import matching reset (ID, usage stats…).
     */
    fun toImportMatch(): Any
}

