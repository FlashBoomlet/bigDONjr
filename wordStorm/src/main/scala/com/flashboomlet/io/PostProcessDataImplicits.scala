package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.db.MongoUtil
import com.flashboomlet.models.PostProcessData
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDouble
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONString

/** implicits for the TweetPostProccessData class */
trait PostProcessDataImplicits extends WordStormMongoConstants {

  implicit object PostProcessDataWriter extends BSONDocumentWriter[PostProcessData] {

    override def write(appd: PostProcessData): BSONDocument = BSONDocument(
      PostProcessDataConstants.EntityLastNameString -> BSONString(appd.entityLastName),
      PostProcessDataConstants.PublishStartDateString -> BSONDateTime(appd.publishStartDate),
      PostProcessDataConstants.IntervalString -> BSONInteger(appd.interval),
      PostProcessDataConstants.Minute -> BSONInteger(appd.minute),
      PostProcessDataConstants.AverageSentimentString -> BSONDouble(appd.averageSentiment),
      PostProcessDataConstants.TotalTitleWordCountString -> BSONInteger(appd.totalTitleWordCount),
      PostProcessDataConstants.ContentCountString -> BSONInteger(appd.contentCount),
      PostProcessDataConstants.TopWordsString -> MongoUtil.mapToBSONDocument(appd.topWords),
      PostProcessDataConstants.TotalSentencesString -> BSONInteger(appd.totalSentences),
      PostProcessDataConstants.TotalWordsString -> BSONInteger(appd.totalWords),
      PostProcessDataConstants.UniqueAuthorsString -> BSONInteger(appd.uniqueAuthors),
      PostProcessDataConstants.PercentPositiveSentiment -> BSONDouble(appd.percentPositiveSentiment),
      PostProcessDataConstants.PercentNegativeSentiment -> BSONDouble(appd.percentNegativeSentiment),
      PostProcessDataConstants.StrategyString -> BSONInteger(appd.strategy)
    )
  }

  implicit object PostProcessDataReader extends BSONDocumentReader[PostProcessData] {

    override def read(doc: BSONDocument): PostProcessData = {
      val entityLastName = doc.getAs[String](PostProcessDataConstants.EntityLastNameString).get
      val publishSD = doc.getAs[Date](PostProcessDataConstants.PublishStartDateString).get.getTime
      val interval = doc.getAs[Int](PostProcessDataConstants.IntervalString).get
      val minute = doc.getAs[Int](PostProcessDataConstants.Minute).get
      val averageSentiment = doc.getAs[Double](PostProcessDataConstants.AverageSentimentString).get
      val titleWordCount = doc.getAs[Int](PostProcessDataConstants.TotalTitleWordCountString).get
      val contentCount = doc.getAs[Int](PostProcessDataConstants.ContentCountString).get
      val topWords = doc.getAs[BSONDocument](PostProcessDataConstants.TopWordsString).get
      val totalSentences = doc.getAs[Int](PostProcessDataConstants.TotalSentencesString).get
      val totalWords = doc.getAs[Int](PostProcessDataConstants.TotalWordsString).get
      val uniqueAuthors = doc.getAs[Int](PostProcessDataConstants.UniqueAuthorsString).get
      val percentPositiveSentiment = doc.getAs[Double](PostProcessDataConstants.PercentPositiveSentiment).get
      val percentNegativeSentiment = doc.getAs[Double](PostProcessDataConstants.PercentNegativeSentiment).get
      val strategy = doc.getAs[Int](PostProcessDataConstants.StrategyString).get

      PostProcessData(
        entityLastName = entityLastName,
        publishStartDate = publishSD,
        interval = interval,
        minute = minute,
        averageSentiment = averageSentiment,
        totalTitleWordCount = titleWordCount,
        contentCount = contentCount,
        topWords = MongoUtil.bsonDocumentToMap(topWords),
        totalSentences = totalSentences,
        totalWords = totalWords,
        uniqueAuthors = uniqueAuthors,
        percentPositiveSentiment = percentPositiveSentiment,
        percentNegativeSentiment = percentNegativeSentiment,
        strategy = strategy
      )
    }
  }
}
