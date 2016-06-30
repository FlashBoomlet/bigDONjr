package com.flashboomlet.io

import com.flashboomlet.db.MongoDatabaseDriver
import com.flashboomlet.db.implicits.MongoImplicits
import com.flashboomlet.models.RecentPostProcess
import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


/**
  * Contains behavior for database functionality
  */
class DatabaseController
    extends WordStormMongoConstants
    with LazyLogging
    with MongoImplicits
    with PostProcessDataImplicits
    with RecentPostProcessImplicits {

  /** Instance of pre-configured database driver. */
  val databaseDriver = MongoDatabaseDriver()

  /** Database collection for article post-process data */
  val articlePostProcessDatasCollection: BSONCollection = databaseDriver
    .db(ArticlePostProcessDatasCollectionString)

  /** Database collection for tweet post process data */
  val tweetPostProcessDatasCollection: BSONCollection = databaseDriver
    .db(TweetPostProcessDatasCollectionString)

  /** Database collection for recent article post process */
  val recentArticlePostProcessCollection: BSONCollection = databaseDriver
    .db(RecentArticlePostProcessCollectionString)

  /** Database collection for recent tweet post process */
  val recentTweetPostProcessCollection: BSONCollection = databaseDriver
    .db(RecentTweetPostProcessCollectionString)

  def getRecentArticlePostProcess(strategy: Int): Option[RecentPostProcess] = {
    val future: Future[Option[RecentPostProcess]] = recentArticlePostProcessCollection
      .find(BSONDocument(RecentPostProcessConstants.StrategyString -> strategy))
      .cursor[RecentPostProcess]().collect[List]()
      .map { list => list.headOption }

    Await.result(future, Duration.Inf)
  }

  def updateRecentArticlePostProcess(recentArticlePostProcess: RecentPostProcess): Unit = {
    val selector = BSONDocument(
      RecentPostProcessConstants.StrategyString -> recentArticlePostProcess.strategy
    )
    val modifier = BSONDocument(GlobalConstants.SetString -> BSONDocument(
      RecentPostProcessConstants.StartTimeString -> recentArticlePostProcess.startTime
    ))
    recentArticlePostProcessCollection.findAndUpdate(selector, modifier, upsert = true)
  }

  def getRecentTweetPostProcess(strategy: Int): Option[RecentPostProcess] = {
    val future: Future[Option[RecentPostProcess]] = recentTweetPostProcessCollection
      .find(BSONDocument(RecentPostProcessConstants.StrategyString -> strategy))
      .cursor[RecentPostProcess]().collect[List]()
      .map { list => list.headOption }

    Await.result(future, Duration.Inf)
  }

  def updateRecentTweetPostProcess(recentTweetPostProcess: RecentPostProcess): Unit = {
    val selector = BSONDocument(
      RecentPostProcessConstants.StrategyString -> recentTweetPostProcess.strategy
    )
    val modifier = BSONDocument(GlobalConstants.SetString -> BSONDocument(
      RecentPostProcessConstants.StartTimeString -> recentTweetPostProcess.startTime
    ))
    recentTweetPostProcessCollection.findAndUpdate(selector, modifier, upsert = true)
  }
}

/**
  * Companion object for the Databse controller
  */
object DatabaseController {

  /** Factory method for the Database Controller */
  def apply(): DatabaseController = new DatabaseController
}
