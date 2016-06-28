package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.models.RecentTweetPostProcess
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONInteger

/** Implicits for the recent post process bookkeeping object */
trait RecentTweetPostProcessImplicits extends WordStormMongoConstants {

  implicit object RecentTweetPostProcessWriter
    extends BSONDocumentWriter[RecentTweetPostProcess] {

    override def write(rtpp: RecentTweetPostProcess): BSONDocument = BSONDocument(
      RecentPostProcessConstants.StartTimeString -> BSONDateTime(rtpp.startTime),
      RecentPostProcessConstants.StrategyString -> BSONInteger(rtpp.strategy)
    )
  }

  implicit object RecentTweetPostProcessReader
    extends BSONDocumentReader[RecentTweetPostProcess] {

    override def read(doc: BSONDocument): RecentTweetPostProcess = {
      val startTime = doc.getAs[Date](RecentPostProcessConstants.StartTimeString).get.getTime
      val strategy = doc.getAs[Int](RecentPostProcessConstants.StrategyString).get

      RecentTweetPostProcess(
        startTime = startTime,
        strategy = strategy
      )
    }
  }

}
