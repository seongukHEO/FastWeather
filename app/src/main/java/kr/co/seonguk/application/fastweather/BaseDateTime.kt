package kr.co.seonguk.application.fastweather

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.LocalTime

data class BaseDateTime (
    val baseDate:String, val baseTime:String
){
    companion object{
        @RequiresApi(Build.VERSION_CODES.O)
        fun getBaseDateTime():BaseDateTime{

            var dateTime = LocalDateTime.now()
            var baseTime = when(dateTime.toLocalTime()){
                //이 코드는 뭐냐하면
                //만약 5월7일 새벽 1시의 경우 아직 2시의 값을 가져오지 못했기 때문에
                //전 날 기준 오후 23시의 값을 가져오라고 알려주는 코드
                in LocalTime.of(0, 0)..LocalTime.of(2, 30) -> {
                    dateTime = dateTime.minusDays(1)
                    "2300"
                }

                in LocalTime.of(2,30)..LocalTime.of(5,30) -> "0200"
                in LocalTime.of(5,30)..LocalTime.of(8,30) -> "0500"
                in LocalTime.of(8,30)..LocalTime.of(11,30) -> "0800"
                in LocalTime.of(11,30)..LocalTime.of(14,30) -> "1100"
                in LocalTime.of(14,30)..LocalTime.of(17,30) -> "1400"
                in LocalTime.of(17,30)..LocalTime.of(20,30) -> "1700"
                in LocalTime.of(20,30)..LocalTime.of(23,30) -> "2000"

                else -> "2300"
            }

            //이걸 왜 하냐면
            //monthValue는 01, 02 이런 식이 아니라 1, 2 이런식으로 받아오기 때문에 사용한다
            val baseDate = String.format("%04d%02d%02d", dateTime.year, dateTime.monthValue, dateTime.dayOfMonth)


            return BaseDateTime(baseDate, baseTime)
        }
    }
}