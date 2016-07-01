package com.flashboomlet.actors

import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.HOURS
import scala.concurrent.duration.FiniteDuration

/**
  * Created by trill on 6/29/16.
  */
object PostProcessAkkaConstants {

  /** Length between schedule ticks for a tweet/article post processor, in hours */
  private[this] val TwentyFourHours = 24

  /** Initial delay for scheduling actors, in milliseconds */
  private[this] val InitialMilliseconds = 5000

  // FINITE DURATIONS //

  /** Finite duration for the initial wait time on actor scheduling */
  val InitialDelay = FiniteDuration(InitialMilliseconds, MILLISECONDS)

  /** Finite duration for the tick time of an ArticlePostProcessTick */
  val OneDayTickLength = FiniteDuration(TwentyFourHours, HOURS)


}
