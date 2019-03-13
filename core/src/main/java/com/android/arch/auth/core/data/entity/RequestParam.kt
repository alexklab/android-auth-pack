package com.android.arch.auth.core.data.entity

open class RequestParam<FieldType : Any>(
    var value: FieldType? = null,
    var isChanged: Boolean = false
) {

    fun reset() {
        value = null
        isChanged = false
    }

    fun edit(valueFrom: FieldType?, valueTo: FieldType?) {
        if (valueFrom != valueTo) {
            value = valueTo
            isChanged = true
        } else {
            reset()
        }
    }

}