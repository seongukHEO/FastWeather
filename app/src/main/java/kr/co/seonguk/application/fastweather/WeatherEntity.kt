package kr.co.seonguk.application.fastweather

data class WeatherEntity(
    val response: Response?
)

data class Items(
    val item: List<Item?>?
)

data class Response(
    val body: Body?,
    val header: Header?
)

data class Body(
    val dataType: String?,
    val items: Items?,
    val numOfRows: Int?,
    val pageNo: Int?,
    val totalCount: Int?
)

data class Header(
    val resultCode: String?,
    val resultMsg: String?
)

data class Item(
    val baseDate: String?,
    val baseTime: String?,
    val category: Category?,
    val fcstDate: String?,
    val fcstTime: String?,
    val fcstValue: String?,
    val nx: Int?,
    val ny: Int?
)