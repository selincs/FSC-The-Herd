package Model

data class Commitment(

    val commitmentId: String = "",

    val activityName: String = "",

    val partnerName: String = "",

    var streak: Int = 0,

    var level: Int = 1,

    val userId: String = "",

    val timestamp: Long = System.currentTimeMillis()
)