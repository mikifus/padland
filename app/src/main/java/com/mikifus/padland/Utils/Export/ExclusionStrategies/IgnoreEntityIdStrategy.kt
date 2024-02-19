package com.mikifus.padland.Utils.Export.ExclusionStrategies

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.mikifus.padland.Database.PadModel.Pad


class IgnoreEntityIdStrategy: ExclusionStrategy {
    override fun shouldSkipField(field: FieldAttributes): Boolean {
        // TODO: Search for PKEY in any class requested
        return field.name == Pad.PKEY
    }

    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return false
    }
}