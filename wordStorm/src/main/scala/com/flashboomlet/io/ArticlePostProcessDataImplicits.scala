package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.db.MongoUtil
import com.flashboomlet.models.ArticlePostProcessData
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDouble
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONString

/** implicits for the TweetPostProccessData class */
trait ArticlePostProcessDataImplicits extends WordStormMongoConstants {

  implicit object ArticlePostProcessDataWriter extends BSONDocumentWriter[ArticlePostProcessData] {

    override def write(appd: ArticlePostProcessData): BSONDocument = BSONDocument(
      PostProcessDataConstants.EntityLastNameString -> BSONString(appd.entityLastName),
      PostProcessDataConstants.PublishStartDateString -> BSONDateTime(appd.publishStartDate),
      PostProcessDataConstants.IntervalString -> BSONInteger(appd.interval),
      PostProcessDataConstants.AverageSentimentString -> BSONDouble(appd.averageSentiment),
      PostProcessDataConstants.TotalTitleWordCountString -> BSONInteger(appd.totalTitleWordCount),
      PostProcessDataConstants.ContentCountString -> BSONInteger(appd.contentCount),
      PostProcessDataConstants.TopWordsString -> MongoUtil.mapToBSONDocument(appd.topWords),
      PostProcessDataConstants.TotalSentencesString -> BSONInteger(appd.totalSentences),
      PostProcessDataConstants.TotalWordsString -> BSONInteger(appd.totalWords),
      PostProcessDataConstants.UniqueAuthorsString -> BSONInteger(appd.uniqueAuthors),
      PostProcessDataConstants.StrategyString -> BSONInteger(appd.strategy)
    )
  }

  implicit object ArticlePostProcessDataReader extends BSONDocumentReader[ArticlePostProcessData] {

    override def read(doc: BSONDocument): ArticlePostProcessData = {
      val entityLastName = doc.getAs[String](PostProcessDataConstants.EntityLastNameString).get
      val publishSD = doc.getAs[Date](PostProcessDataConstants.PublishStartDateString).get.getTime
      val interval = doc.getAs[Int](PostProcessDataConstants.IntervalString).get
      val averageSentiment = doc.getAs[Double](PostProcessDataConstants.AverageSentimentString).get
      val titleWordCount = doc.getAs[Int](PostProcessDataConstants.TotalTitleWordCountString).get
      val contentCount = doc.getAs[Int](PostProcessDataConstants.ContentCountString).get
      val topWords = doc.getAs[BSONDocument](PostProcessDataConstants.TopWordsString).get
      val totalSentences = doc.getAs[Int](PostProcessDataConstants.TotalSentencesString).get
      val totalWords = doc.getAs[Int](PostProcessDataConstants.TotalWordsString).get
      val uniqueAuthors = doc.getAs[Int](PostProcessDataConstants.UniqueAuthorsString).get
      val strategy = doc.getAs[Int](PostProcessDataConstants.StrategyString).get

      ArticlePostProcessData(
        entityLastName = entityLastName,
        publishStartDate = publishSD,
        interval = interval,
        averageSentiment = averageSentiment,
        totalTitleWordCount = titleWordCount,
        contentCount = contentCount,
        topWords = MongoUtil.bsonDocumentToMap(topWords),
        totalSentences = totalSentences,
        totalWords = totalWords,
        uniqueAuthors = uniqueAuthors,
        strategy = strategy
      )
    }
  }
}
