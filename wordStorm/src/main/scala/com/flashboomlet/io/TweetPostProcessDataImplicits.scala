package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.db.MongoUtil
import com.flashboomlet.models.TweetPostProcessData
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONDouble
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONString

/** implicits for the TweetPostProccessData class */
trait TweetPostProcessDataImplicits extends WordStormMongoConstants {

  implicit object TweetPostProcessDataWriter extends BSONDocumentWriter[TweetPostProcessData] {

    override def write(tppd: TweetPostProcessData): BSONDocument = BSONDocument(
      PostProcessDataConstants.EntityLastNameString -> BSONString(tppd.entityLastName),
      PostProcessDataConstants.PublishStartDateString -> BSONDateTime(tppd.publishStartDate),
      PostProcessDataConstants.IntervalString -> BSONInteger(tppd.interval),
      PostProcessDataConstants.AverageSentimentString -> BSONDouble(tppd.averageSentiment),
      PostProcessDataConstants.TotalTitleWordCountString -> BSONInteger(tppd.totalTitleWordCount),
      PostProcessDataConstants.ContentCountString -> BSONInteger(tppd.contentCount),
      PostProcessDataConstants.TopWordsString -> MongoUtil.mapToBSONDocument(tppd.topWords),
      PostProcessDataConstants.TotalSentencesString -> BSONInteger(tppd.totalSentences),
      PostProcessDataConstants.TotalWordsString -> BSONInteger(tppd.totalWords),
      PostProcessDataConstants.UniqueAuthorsString -> BSONInteger(tppd.uniqueAuthors)
    )
  }

  implicit object TweetPostProcessDataReader extends BSONDocumentReader[TweetPostProcessData] {

    override def read(doc: BSONDocument): TweetPostProcessData = {
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

      TweetPostProcessData(
        entityLastName = entityLastName,
        publishStartDate = publishSD,
        interval = interval,
        averageSentiment = averageSentiment,
        totalTitleWordCount = titleWordCount,
        contentCount = contentCount,
        topWords = MongoUtil.bsonDocumentToMap(topWords),
        totalSentences = totalSentences,
        totalWords = totalWords,
        uniqueAuthors = uniqueAuthors
      )
    }
  }
}
