package com.flashboomlet.proccessing

import java.util.Date

import com.flashboomlet.io.DatabaseController
import com.flashboomlet.models.RecentArticlePostProcess
import com.flashboomlet.models.RecentTweetPostProcess
import com.flashboomlet.preproccessing.DateUtil

/**
  * Created by trill on 6/28/16.
  */
object PostProcessingUtil {

  /** THIS IS LOCAL TIME, WHICH IS WHAT ALL TWEET TIMESTAMPS ARE IN. ARTICLE PUBLISH DATE TIME
    * ZONE NEEDS INVESTIGATION. ARTICLES ARE GMT AND SHOULD BE UPDATED TO LOCAL TIME */
  val beginningOfTime = DateUtil.getNytInMillis("2016-06-25T09:00:00Z")

  def getRecentArticlePostProcess(
    strategy: Int)(implicit dbController: DatabaseController): RecentArticlePostProcess = {

    dbController.getRecentArticlePostProcess(strategy) match {
      case Some(r) => r
      case None =>
        val startTime = beginningOfTime
        val recentArticlePostProcess = RecentArticlePostProcess(
          startTime = startTime,
          strategy = strategy)
        dbController.updateRecentArticlePostProcess(recentArticlePostProcess)
        recentArticlePostProcess
    }
  }

  def getRecentTweetPostProcess(
      strategy: Int)(implicit dbController: DatabaseController): RecentTweetPostProcess = {

    dbController.getRecentTweetPostProcess(strategy) match {
      case Some(r) => r
      case None =>
        val startTime = beginningOfTime
        val recentArticlePostProcess = RecentTweetPostProcess(
          startTime = startTime,
          strategy = strategy)
        dbController.updateRecentTweetPostProcess(recentArticlePostProcess)
        recentArticlePostProcess
    }
  }
}
