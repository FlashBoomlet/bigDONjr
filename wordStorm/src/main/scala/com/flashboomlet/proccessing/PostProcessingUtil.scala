package com.flashboomlet.proccessing

import com.flashboomlet.io.DatabaseController
import com.flashboomlet.models.RecentPostProcess
import com.flashboomlet.preproccessing.DateUtil

/**
  * Created by trill on 6/28/16.
  */
object PostProcessingUtil {

  /** THIS IS LOCAL TIME, WHICH IS WHAT ALL TWEET TIMESTAMPS ARE IN. ARTICLE PUBLISH DATE TIME
    * ZONE NEEDS INVESTIGATION. ARTICLES ARE GMT AND SHOULD BE UPDATED TO LOCAL TIME */
  val beginningOfTime = DateUtil.getNytInMillis("2016-06-25T09:00:00Z")

  def getRecentArticlePostProcess(
    strategy: Int)(implicit dbController: DatabaseController): RecentPostProcess = {

    dbController.getRecentArticlePostProcess(strategy) match {
      case Some(r) => r
      case None =>
        val startTime = beginningOfTime
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
        val startTime = beginningOfTime
        val recentTweetPostProcess = RecentPostProcess(
          startTime = startTime,
          strategy = strategy)
        dbController.updateRecentTweetPostProcess(recentTweetPostProcess)
        recentTweetPostProcess
    }
  }
}
