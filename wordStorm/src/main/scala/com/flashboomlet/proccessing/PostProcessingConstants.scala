package com.flashboomlet.proccessing

import com.flashboomlet.preproccessing.DateUtil

/**
  * Created by trill on 6/30/16.
  */
object PostProcessingConstants {


  /** THIS IS LOCAL TIME, WHICH IS WHAT ALL TWEET TIMESTAMPS ARE IN. ARTICLE PUBLISH DATE TIME
    * ZONE NEEDS INVESTIGATION. ARTICLES ARE GMT AND SHOULD BE UPDATED TO LOCAL TIME */
  val BeginningOfTime = DateUtil.getNytInMillis("2016-06-25T09:00:00Z")

  final val NewYorkTimesIntervalMinutes = 30

  final val TwitterIntervalMinutes = 5

  val Strategy = 0

  final val TopWordCount: Int = 10

  final val PositiveString = "Positive"

  final val NegativeString = "Negative"

  final val NeutralString = "Neutral"

  /** When a double or float is divided by this number it will become a percentage */
  final val ToPercent = 100.0
}
