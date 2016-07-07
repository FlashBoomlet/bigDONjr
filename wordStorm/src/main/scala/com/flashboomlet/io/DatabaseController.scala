package com.flashboomlet.io

import com.flashboomlet.data.models.FinalTweet
import com.flashboomlet.data.models.NewYorkTimesArticle
import com.flashboomlet.db.MongoDatabaseDriver
import com.flashboomlet.db.implicits.MongoImplicits
import com.flashboomlet.models.PostProcessData
import com.flashboomlet.models.RecentPostProcess
import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success


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

//  def dumpPostProcessData(): Unit = {
//    Await.result(
//      articlePostProcessDatasCollection.find(BSONDocument()).cursor[PostProcessData]().collect[List](),
//      Duration.Inf).foreach(d => println(d))
//  }

  def insertArticlePostProcessData(postProcessData: PostProcessData): Unit = {
    articlePostProcessDatasCollection.insert(postProcessData).onComplete {
      case Failure(e) =>
        logger.error(s"Failed to insert article PostProcessData: ${e.getMessage}")
        throw e// we fucked up
      case Success(writeResult) => logger.info("Successfully inserted article PostProcessData")
    }
  }

  def insertTweetPostProcessData(postProcessData: PostProcessData): Unit = {
    tweetPostProcessDatasCollection.insert(postProcessData).onComplete {
      case Failure(e) =>
        logger.error(s"Failed to insert tweet PostProcessData: ${e.getMessage}")
        throw e// we fucked up
      case Success(writeResult) => logger.info("Successfully inserted tweet PostProcessData")
    }
  }

  def getMetaDataDateRangeQuery(start: Long, end: Long): BSONDocument = BSONDocument(
    GlobalConstants.MetaDatasString ->
      BSONDocument(GlobalConstants.ElemMatchString ->
        BSONDocument(
          MetaDataConstants.PublishDateString -> BSONDocument(
            "$gte" -> BSONDateTime(start),
            "$lt" -> BSONDateTime(end)
          )
        )
      )
  )

  def getPostProcessDateRangeQuery(entity: String, start: Long, end: Long): BSONDocument =
    BSONDocument(
      PostProcessDataConstants.EntityLastNameString -> entity/*,
      PostProcessDataConstants.PublishStartDateString ->
        BSONDocument(
          MetaDataConstants.PublishDateString -> BSONDocument(
            "$gte" -> BSONDateTime(start),
            "$lt" -> BSONDateTime(end)
          ) */
        //)
    )

  def dumpPostProcesses: Unit = {
    Await.result(tweetPostProcessDatasCollection.find(BSONDocument()).cursor[PostProcessData]().collect[List](), Duration.Inf).foreach(d => println(d))
    Await.result(articlePostProcessDatasCollection.find(BSONDocument()).cursor[PostProcessData]().collect[List](), Duration.Inf).foreach(d => println(d))

  }

  def dumpRecentPostProcess: Unit = {
    Await.result(recentTweetPostProcessCollection.find(BSONDocument()).cursor[RecentPostProcess]().collect[List](), Duration.Inf).foreach(d => println(d))
    Await.result(recentArticlePostProcessCollection.find(BSONDocument()).cursor[RecentPostProcess]().collect[List](), Duration.Inf).foreach(d => println(d))

  }

  def getArticlePostProcesses(
     entityLastName: String,
     starTime: Long,
     endTime: Long): List[PostProcessData] = {

   val future: Future[List[PostProcessData]] = articlePostProcessDatasCollection.find(
     getPostProcessDateRangeQuery(entityLastName, starTime, endTime)
   ).cursor[PostProcessData]().collect[List]()

   Await.result(future, Duration.Inf)
  }


  def getTweetPostProcesses(
      entityLastName: String,
      starTime: Long,
      endTime: Long): List[PostProcessData] = {

    val future: Future[List[PostProcessData]] = tweetPostProcessDatasCollection.find(
      getPostProcessDateRangeQuery(entityLastName, starTime, endTime)
    ).cursor[PostProcessData]().collect[List]()

    Await.result(future, Duration.Inf)
  }

 def getTweets(startTime: Long, endTime: Long): List[FinalTweet] = {
   val future: Future[List[FinalTweet]] = databaseDriver.tweetsCollection
     .find(getMetaDataDateRangeQuery(startTime, endTime))
     .cursor[FinalTweet]().collect[List]()

   Await.result(future, Duration.Inf)
 }

  def getArticles(startTime: Long, endTime: Long): List[NewYorkTimesArticle] = {
    val future: Future[List[NewYorkTimesArticle]] = databaseDriver.newYorkTimesArticlesCollection
      .find(getMetaDataDateRangeQuery(startTime, endTime))
      .cursor[NewYorkTimesArticle]().collect[List]()

    Await.result(future, Duration.Inf)
  }

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
