package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.models.RecentPostProcess
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONInteger

/** Implicits for the recent post process bookkeeping object */
trait RecentPostProcessImplicits extends WordStormMongoConstants {

  implicit object RecentPostProcessWriter extends BSONDocumentWriter[RecentPostProcess] {

    override def write(rapp: RecentPostProcess): BSONDocument = BSONDocument(
      RecentPostProcessConstants.StartTimeString -> BSONDateTime(rapp.startTime),
      RecentPostProcessConstants.StrategyString -> BSONInteger(rapp.strategy)
    )
  }

  implicit object RecentPostProcessReader extends BSONDocumentReader[RecentPostProcess] {

    override def read(doc: BSONDocument): RecentPostProcess = {
      val startTime = doc.getAs[Date](RecentPostProcessConstants.StartTimeString).get.getTime
      val strategy = doc.getAs[Int](RecentPostProcessConstants.StrategyString).get

      RecentPostProcess(
        startTime = startTime,
        strategy = strategy
      )
    }
  }

}
