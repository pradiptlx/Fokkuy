package com.fpradipt.fokkuy.model

class UsageFirestoreModel(
    var id: String? = null,
    var startTimer: Long = 0L,
    var endTimer: Long = 0L,
    var duration: Int = 0,
    var createdAt: String? = null
) {

    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["start_timer"] = startTimer
        result["end_timer"] = endTimer
        result["duration"] = duration
        result["created_at"] = createdAt?:""

        return result
    }
}