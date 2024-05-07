package kr.co.seonguk.application.fastweather

data class Forecast (
    val fcstDate:String?,
    val fcstTime:String?,

    var temperature:Double = 0.0,
    var sky:String = "",
    var precipitation: Int = 0,
    var precipitationType:String = ""
)