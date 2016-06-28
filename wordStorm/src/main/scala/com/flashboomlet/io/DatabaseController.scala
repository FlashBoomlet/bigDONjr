package com.flashboomlet.io

import com.flashboomlet.data.models.FinalTweet
import com.flashboomlet.data.models.NewYorkTimesArticle
import com.flashboomlet.db.MongoDatabaseDriver
import com.flashboomlet.db.implicits.MongoImplicits
import com.flashboomlet.models.RecentArticlePostProcess
import com.flashboomlet.models.RecentTweetPostProcess
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
    with ArticlePostProcessDataImplicits
    with TweetPostProcessDataImplicits
    with RecentArticlePostProcessImplicits
    with RecentTweetPostProcessImplicits {

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

  def getRecentArticlePostProcess(strategy: Int): Option[RecentArticlePostProcess] = {
    val future: Future[Option[RecentArticlePostProcess]] = recentArticlePostProcessCollection
      .find(BSONDocument(RecentPostProcessConstants.StrategyString -> strategy))
      .cursor[RecentArticlePostProcess]().collect[List]()
      .map { list => list.headOption }

    Await.result(future, Duration.Inf)
  }

  def updateRecentArticlePostProcess(recentArticlePostProcess: RecentArticlePostProcess): Unit = {
    val selector = BSONDocument(
      RecentPostProcessConstants.StrategyString -> recentArticlePostProcess.strategy
    )
    val modifier = BSONDocument(GlobalConstants.SetString -> BSONDocument(
      RecentPostProcessConstants.StartTimeString -> recentArticlePostProcess.startTime
    ))
    recentArticlePostProcessCollection.findAndUpdate(selector, modifier, upsert = true)
  }

  def getRecentTweetPostProcess(strategy: Int): Option[RecentTweetPostProcess] = {
    val future: Future[Option[RecentTweetPostProcess]] = recentTweetPostProcessCollection
      .find(BSONDocument(RecentPostProcessConstants.StrategyString -> strategy))
      .cursor[RecentTweetPostProcess]().collect[List]()
      .map { list => list.headOption }

    Await.result(future, Duration.Inf)
  }

  def updateRecentTweetPostProcess(recentTweetPostProcess: RecentTweetPostProcess): Unit = {
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
