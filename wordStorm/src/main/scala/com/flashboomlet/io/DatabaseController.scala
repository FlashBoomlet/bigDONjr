package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.data.models.FinalTweet
import com.flashboomlet.data.models.NewYorkTimesArticle
import com.flashboomlet.data.models.PollsterDataPoint
import com.flashboomlet.data.models.TwitterSearch
import com.flashboomlet.db.MongoDatabaseDriver
import com.flashboomlet.db.MongoConstants
import com.flashboomlet.db.implicits.MongoImplicits
import com.flashboomlet.models.PostProcessData
import com.flashboomlet.models.RecentPostProcess
import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONString

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
    with MongoConstants
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

  val twitterSearchesCollection: BSONCollection = databaseDriver.db(TwitterSearchesCollection)

  /** Database collection for recent article post process */
  val recentArticlePostProcessCollection: BSONCollection = databaseDriver
    .db(RecentArticlePostProcessCollectionString)

  /** Database collection for recent tweet post process */
  val recentTweetPostProcessCollection: BSONCollection = databaseDriver
    .db(RecentTweetPostProcessCollectionString)

  val pollsterDataPointsCollection: BSONCollection = databaseDriver
    .db(PollsterDataPointsCollection)

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
      PostProcessDataConstants.EntityLastNameString -> entity/*
      PostProcessDataConstants.PublishStartDateString ->
        BSONDocument(
          MetaDataConstants.PublishDateString -> BSONDocument(
            "$gte" -> BSONDateTime(start),
            "$lt" -> BSONDateTime(end)
          ) */
        //)
    )

  def dumpPostProcesses: Unit = {
    Await.result(tweetPostProcessDatasCollection.find(BSONDocument()).cursor[PostProcessData]()
      .collect[List](), Duration.Inf).foreach(d => println(d))
    Await.result(articlePostProcessDatasCollection.find(BSONDocument()).cursor[PostProcessData]()
      .collect[List](), Duration.Inf).foreach(d => println(d))
  }

  def dumpRecentPostProcess: Unit = {
    print("Tweets: ")
    Await.result(recentTweetPostProcessCollection.find(BSONDocument()).cursor[RecentPostProcess]()
      .collect[List](), Duration.Inf).foreach(d => println(d))
    print("Articles: ")
    Await.result(recentArticlePostProcessCollection.find(BSONDocument()).cursor[RecentPostProcess]()
      .collect[List](), Duration.Inf).foreach(d => println(d))
  }

  def dumpRecentTweet: Unit = {
    Await.result(databaseDriver.twitterSearchesCollection.find(BSONDocument())
      .cursor[TwitterSearch]().collect[List](), Duration.Inf).foreach(d => println(d))
  }

  /**
    * Gets a twitter search recent tweet id from a given query and entity last name
    *
    * @param query query to search for relevent TwitterSearch in DB
    * @param entityLastName Entity last name to search for relevent TwitterSearch in DB
    * @return Recent tweet id associated with the TwitterSearch if it exists, else none
    */
  def getTwitterSearch(query: String, entityLastName: String): Option[TwitterSearch] ={
    val future: Future[Option[TwitterSearch]] = twitterSearchesCollection
    .find(BSONDocument(
      TwitterSearchConstants.QueryString -> query,
      TwitterSearchConstants.EntityLastNameString -> entityLastName))
    .cursor[TwitterSearch]().collect[List]()
    .map { list => list.headOption }
    Await.result(future, Duration.Inf)
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

  def getPollsterDataPointData(
    starTime: Long = 1432015200000L,
    endTime: Long = new Date().getTime): List[PollsterDataPoint] = {

    val future: Future[List[PollsterDataPoint]] = pollsterDataPointsCollection.find(
      BSONDocument(
        PollsterDataPointConstants.DateString -> BSONDocument(
          "$gte" -> BSONDateTime(starTime),
          "$lt" -> BSONDateTime(endTime)
        )
      )
    ).cursor[PollsterDataPoint]().collect[List]()

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
