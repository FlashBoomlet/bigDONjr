package com.flashboomlet.proccessing


import com.fasterxml.jackson.databind.ObjectMapper
import com.flashboomlet.data.models.FinalTweet
import com.flashboomlet.data.models.NewYorkTimesArticle
import com.flashboomlet.data.models.Sentiment
import com.flashboomlet.models.PostProcessData
import com.flashboomlet.models.RawData


/**
  * Post Processing Handles all of the post processing of the data.
  *
  * Created by ttlynch on 6/28/16.
  */
class PostProcessing {

  val topWordCount: Int = 10

  /**
    * Post Process Tweets is a function to post process the Tweets
    *
    * @param startDate the start data in MS of the group of tweets
    * @param interval the length of the interval that the tweets are from
    * @param tweets a list of final tweets
    */
  def postProcessTweets(
    startDate: Long,
    interval: Int,
    tweets: List[FinalTweet]): Unit = {

    val processedTweets: Seq[PostProcessData] = postProcess(
      tweetsToRawData(tweets),
      startDate,
      interval)
    // TODO: Insert into database in the Tweet Post Process
  }

  /**
    * Post Process NYT Articles is a function to post process the NYT articles
    *
    * @param startDate the start data in MS of the group of the articles
    * @param interval the length of the interval that the articles are from
    * @param articles a list of new york times articles
    */
  def postProcessNYTArticles(
    startDate: Long,
    interval: Int,
    articles: List[NewYorkTimesArticle]): Unit = {

    val processedNYTArticles: Seq[PostProcessData] = postProcess(
      nytArticlesToRawData(articles),
      startDate,
      interval)
    // TODO: Insert into database in the Article Post Process
  }

  /**
    * Post Process is a function to process the content for the generic time series counts and
    *   overall high level temporal data
    *
    * @param rawData the raw data to process
    * @param startDate the start date for the content
    * @param interval the interval for the content
    * @return a sequence of post processed data for each entity
    */
  private def postProcess(
     rawData: List[RawData],
     startDate: Long,
     interval: Int): Seq[PostProcessData] = {

    aggregateRawData(rawData, startDate, interval)
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
        totalWords = group._2.map(_.wordCount).sum,
        totalSentences = group._2.map(_.sentenceCount).sum,
        totalTitleWordCount = group._2.map(_.titleWordCount).sum,
        contentCount = group._2.length,
        topWords = topWords(group._2.map(_.wordOccurrences) reduce (_ ++ _)),
        uniqueAuthors = group._2.map(_.author).distinct.length,
        averageSentiment = calculateAverageSentiment(group._2.map(_.sentiment)),
        strategy = 0
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
    Map(words.toSeq.sortWith(_._2 > _._2):_*).take(topWordCount)
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
    val positive = getSumsAndCounts(sentiment.filter(s => s.result == "Positive"))
    val negative = getSumsAndCounts(sentiment.filter(s => s.result == "Negative"))
    val neutral = getSumsAndCounts(sentiment.filter(s => s.result == "Neutral"))
    val w1 = (positive._1 - negative._1) / (positive._2 + negative._2)
    val w2 = neutral._1 / neutral._2
    val w3 = neutral._2 / ((positive._2 + negative._2) + neutral._2)
    w1 * (1.0 - (w2 * Math.pow(w3, goldenRatio)))
  }

  /**
    * Get Sums and Counts is a helper function to get the sums and counts of a sentiment
    * @param sentiment the list of sentiment values to derive data on
    * @return the derived data, sums and counts
    */
  private def getSumsAndCounts(sentiment: List[Sentiment]): (Double, Double) = {
    val toPercent = 100.0
    val sumAndCounts = sentiment.foldLeft[(Double, Double)]((0,0))( (acc, data) => {
      (acc._1 + data.confidence.toDouble, acc._2 + 1.0)
    })
    (sumAndCounts._1 / toPercent, sumAndCounts._2)
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

/**
  * Companion object for the Post Processor
  */
object PostProcessing {
  def apply(): PostProcessing = {
    new PostProcessing()
  }
}
