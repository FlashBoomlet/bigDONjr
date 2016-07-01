package com.flashboomlet.proccessing

import com.flashboomlet.io.DatabaseController
import com.flashboomlet.models.RecentPostProcess

/**
  * Created by trill on 6/28/16.
  */
object PostProcessingUtil {

  def getRecentArticlePostProcess(
    strategy: Int)(implicit dbController: DatabaseController): RecentPostProcess = {

    dbController.getRecentArticlePostProcess(strategy) match {
      case Some(r) => r
      case None =>
        val startTime = PostProcessingConstants.BeginningOfTime
        val recentArticlePostProcess = RecentPostProcess(
          startTime = startTime,
          strategy = strategy)
        dbController.updateRecentArticlePostProcess(recentArticlePostProcess)
        recentArticlePostProcess
    }
  }

  def getRecentTweetPostProcess(
      strategy: Int)(implicit dbController: DatabaseController): RecentPostProcess = {

    dbController.getRecentTweetPostProcess(strategy) match {
      case Some(r) => r
      case None =>
        val startTime = PostProcessingConstants.BeginningOfTime
        val recentTweetPostProcess = RecentPostProcess(
          startTime = startTime,
          strategy = strategy)
        dbController.updateRecentTweetPostProcess(recentTweetPostProcess)
        recentTweetPostProcess
    }
  }

  /**
    * Converts a minute interval to milliseconds
    * @param minutes interval in minutes
    * @return interval in milliseconds
    */
  def getIntervalMillis(minutes: Int): Long = minutes * 60 * 1000 // scalastyle:ignore magic.number
}
