package com.flashboomlet.proccessing

import java.text.SimpleDateFormat

import com.flashboomlet.WordStormDriver
import com.flashboomlet.data.models.FinalTweet
import com.flashboomlet.data.models.NewYorkTimesArticle
import com.flashboomlet.data.models.Sentiment
import com.flashboomlet.models.PostProcessData
import com.flashboomlet.models.RawData
import com.flashboomlet.models.RecentPostProcess
import com.flashboomlet.preproccessing.DateUtil
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec


/**
  * Post Processing Handles all of the post processing of the data.
  *
  * Created by ttlynch on 6/28/16.
  */
object PostProcessor extends LazyLogging {

  /**
    * Post Process NYT Articles is a function to post process the NYT articles
    *
    * Retrieves the start point to start the recursive call. The end point is the time this function
    * was called. This is the exit condition for the inner recursive function.
    */
  def postProcessTweets(): Unit = {
    val beginning = PostProcessingUtil.getRecentTweetPostProcess(PostProcessingConstants.Strategy)(
        WordStormDriver.databaseController)
      .startTime
    postProcessTweetsRecursive(beginning, DateUtil.getNowInMillis)
  }

  /**
    * Post Process NYT Articles is a function to post process the NYT articles
    *
    * Retrieves the start point to start the recursive call. The end point is the time this function
    * was called. This is the exit condition for the inner recursive function.
    */
  def postProcessNYTArticles(): Unit = {
    val beginning = PostProcessingUtil.getRecentArticlePostProcess(
        PostProcessingConstants.Strategy)(WordStormDriver.databaseController)
      .startTime
    postProcessNYTArticlesRecursive(beginning, DateUtil.getNowInMillis)
  }

  /**
    * Tail recursively fetches interval chunks of data, post processes them, and inserts them
    * into the database.
    *
    * The exist condition is the cycleFinish. When the next interval passes the finalCycle, the
    * function returns.
    *
    * @param startDate The start of the current chunk time frame
    * @param cycleFinish the end of the entire cycle of chunks
    * @param interval the interval for each chunk, in minutes
    */
  @tailrec
  private[this] def postProcessTweetsRecursive(
      startDate: Long,
      cycleFinish: Long,
      interval: Int = PostProcessingConstants.TwitterIntervalMinutes): Unit = {

    if (startDate + PostProcessingUtil.getIntervalMillis(interval) < cycleFinish) {
      val intervalMillis = PostProcessingUtil.getIntervalMillis(interval)
      val endDate: Long = startDate + intervalMillis
      val finalTweets: List[FinalTweet] =
        WordStormDriver.databaseController.getTweets(startDate, endDate)

      val postProcessDatas: Seq[PostProcessData] = aggregateRawData(
        tweetsToRawData(finalTweets),
        startDate,
        interval)

      postProcessDatas.foreach(d =>
        WordStormDriver.databaseController.insertTweetPostProcessData(d)
      )
      val newRecentPostProcess = RecentPostProcess(startDate, PostProcessingConstants.Strategy)
      WordStormDriver.databaseController.updateRecentTweetPostProcess(newRecentPostProcess)
      postProcessTweetsRecursive(endDate, cycleFinish, interval)
    } else {
      val nextRecentPostProcess = RecentPostProcess(startDate, PostProcessingConstants.Strategy)
      WordStormDriver.databaseController.updateRecentTweetPostProcess(nextRecentPostProcess)
    }
  }


  /**
    * Tail recursively fetches interval chunks of data, post processes them, and inserts them
    * into the database.
    *
    * The exist condition is the cycleFinish. When the next interval passes the finalCycle, the
    * function returns.
    *
    * @param startDate The start of the current chunk time frame
    * @param cycleFinish the end of the entire cycle of chunks
    * @param interval the interval for each chunk, in minutes
    */
  @tailrec
  private[this] def postProcessNYTArticlesRecursive(
      startDate: Long,
      cycleFinish: Long,
      interval: Int = PostProcessingConstants.NewYorkTimesIntervalMinutes): Unit = {

    if (startDate + PostProcessingUtil.getIntervalMillis(interval) < cycleFinish) {
      val intervalMillis = PostProcessingUtil.getIntervalMillis(interval)
      val endDate: Long = startDate + intervalMillis
      val newYorkTimesArticles: List[NewYorkTimesArticle] =
        WordStormDriver.databaseController.getArticles(startDate, endDate)

      val postProcessDatas: Seq[PostProcessData] = aggregateRawData(
        nytArticlesToRawData(newYorkTimesArticles),
        startDate,
        interval)

      postProcessDatas.foreach(d =>
        WordStormDriver.databaseController.insertArticlePostProcessData(d)
      )
      val newRecentPostProcess = RecentPostProcess(startDate, PostProcessingConstants.Strategy)
      WordStormDriver.databaseController.updateRecentArticlePostProcess(newRecentPostProcess)
      postProcessNYTArticlesRecursive(endDate, cycleFinish, interval)
    } else {
      val nextRecentPostProcess = RecentPostProcess(startDate, PostProcessingConstants.Strategy)
      WordStormDriver.databaseController.updateRecentArticlePostProcess(nextRecentPostProcess)
    }
  }

  /**
    * Counts by time group process all content and aggregates it into time cluster temporal data
    *
    * @param rawData raw data to be grouped by a given time group (ex: 5 minute clusters)
    * @return TimeGroupData that stores aggregated data by time group
    */
  private def aggregateRawData(
    rawData: List[RawData],
    startDate: Long,
    interval: Int): Seq[PostProcessData] = {

    rawData.groupBy(data => data.entityLastName).map(group =>
      PostProcessData(
        entityLastName = group._1,
        publishStartDate = startDate,
        interval = interval,
        minute = msTimeToMinuteOfDay(startDate),
        totalWords = group._2.map(_.wordCount).sum,
        totalSentences = group._2.map(_.sentenceCount).sum,
        totalTitleWordCount = group._2.map(_.titleWordCount).sum,
        contentCount = group._2.length,
        topWords = topWords(combinedMaps(group._2.map(_.wordOccurrences))),
        uniqueAuthors = group._2.map(_.author).distinct.length,
        averageSentiment = calculateAverageSentiment(group._2.map(_.sentiment)),
        percentPositiveSentiment = percentSentiment(group._2.map(_.sentiment),
          PostProcessingConstants.PositiveString),
        percentNegativeSentiment = percentSentiment(group._2.map(_.sentiment),
          PostProcessingConstants.NegativeString),
        strategy = PostProcessingConstants.Strategy
      )
    ).toSeq
  }

  /**
    * Top words is a helper function to sort the top words and then take the top of the top words
    *
    * @param words the words to be sorted
    * @return the top of the top words
    */
  private def topWords(words: Map[String, Int]): Map[String, Int] = {
    Map(words.toSeq.sortWith(_._2 > _._2): _*).take(PostProcessingConstants.TopWordCount)
  }

  /**
    * Helper function to combined Maps
    *
    * @param data a list of maps to combined and accumulate
    * @return a combined map
    */
  private def combinedMaps(data: List[Map[String, Int]]): Map[String, Int] = {
    data.flatMap(mapObj => mapObj.toList).groupBy(_._1).map(item =>
      (item._1, item._2.map(_._2).sum)
    )
  }

  /**
    * MS Time to Minute of Day Converts a MS timestamp to a 5 minute interval in a day
    *
    * @param time the MS time stamp
    * @return the int of the 5 minute interval in a day that the MS time stamp is in
    */
  def msTimeToMinuteOfDay(time: Long): Int = {
    val minuteFormat = new SimpleDateFormat("mm")
    val hourFormat = new SimpleDateFormat("HH")
    val minute = Math.floor(minuteFormat.format(time).toInt / 5 ) * 5
    val hour = hourFormat.format(time).toInt * 60
    hour + minute.toInt
  }

  /**
    * Calculate Average Sentiment is a helper function to calculate the average sentiment.
    *
    * This function finds the sum of the sums up each category and then divides by the total of all
    *   of the data
    *
    * @param sentiment a list of all of the sentiment
    * @return a double of the average sentiment
    */
  def calculateAverageSentiment(sentiment: List[Sentiment]): Double = {
    val goldenRatio = 1.618
    val positive = getSumsAndCounts(
      sentiment.filter(s => s.result == PostProcessingConstants.PositiveString))
    val negative = getSumsAndCounts(
      sentiment.filter(s => s.result == PostProcessingConstants.NegativeString))
    val neutral = getSumsAndCounts(
      sentiment.filter(s => s.result == PostProcessingConstants.NeutralString))
    if (positive._2 + negative._2 != 0) {
      val w1 = (positive._1 - negative._1) / (positive._2 + negative._2)
      if (neutral._2 != 0) {
        val w2 = neutral._1 / neutral._2
        val w3 = neutral._2 / ((positive._2 + negative._2) + neutral._2)
        w1 * (1.0 - (w2 * Math.pow(w3, goldenRatio)))
      } else {
        w1 // we have no neutral sentiments
      }
    } else {
      0.0 // if there are no positive or negative sentiments, we have pure neutrality
    }
  }

  /**
    * Calculates the Percent of content that falls within a given sentiment level
    *
    * @param sentiment the list of sentiment values
    * @param sign the sentiment level
    * @return a percent of the content that is at the given sentiment level
    */
  def percentSentiment(sentiment: List[Sentiment], sign: String): Double = {
    val positive = getSumsAndCounts(
      sentiment.filter(s => s.result == sign))
    if(sentiment.isEmpty) {
      0D
    }
    else
    {
      positive._2 / sentiment.length
    }
  }


  /**
    * Get Sums and Counts is a helper function to get the sums and counts of a sentiment
    *
    * @param sentiment the list of sentiment values to derive data on
    * @return the derived data, sums and counts (Sum of Sentiment, Count of Sentiment)
    */
  private def getSumsAndCounts(sentiment: List[Sentiment]): (Double, Double) = {
    val sumAndCounts = sentiment.foldLeft[(Double, Double)]((0,0))( (acc, data) => {
      (acc._1 + data.confidence.toDouble, acc._2 + 1.0)
    })
    (sumAndCounts._1 / PostProcessingConstants.ToPercent, sumAndCounts._2)
  }

  /**
    * tweets to raw data converts a list of tweets to a raw data format to find features in
    *
    * @param data the data stored in the database
    * @return the raw, useful data
    */
  private def tweetsToRawData(data: List[FinalTweet]): List[RawData] = {
    data.flatMap { point =>
      point.metaDatas.map(_.entityLastName).toList.distinct.map { entity =>
        RawData(
          entityLastName = entity,
          sentiment = point.preprocessData.sentiment,
          wordCount = point.preprocessData.counts.wordCount,
          sentenceCount = point.preprocessData.counts.sentenceCount,
          titleWordCount = point.preprocessData.counts.titleWordCount,
          wordOccurrences = point.preprocessData.counts.wordOccurrences,
          author = point.userID.toString
        )
      }
    }
  }

  /**
    * nyt articles to raw data converts a list of nyt articles to a raw data format to find
    *   features in
    *
    * @param data the data stored in the database
    * @return the raw, useful data
    */
  private def nytArticlesToRawData(data: List[NewYorkTimesArticle]): List[RawData] = {
    data.flatMap { point =>
      point.metaDatas.map(_.entityLastName).toList.distinct.map { entity =>
        RawData(
          entityLastName = entity,
          sentiment = point.preprocessData.sentiment,
          wordCount = point.preprocessData.counts.wordCount,
          sentenceCount = point.preprocessData.counts.sentenceCount,
          titleWordCount = point.preprocessData.counts.titleWordCount,
          wordOccurrences = point.preprocessData.counts.wordOccurrences,
          author = point.author
        )
      }
    }
  }

}
