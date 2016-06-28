package com.flashboomlet.io

import java.util.Date

import com.flashboomlet.models.RecentArticlePostProcess
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import reactivemongo.bson.BSONInteger

/** Implicits for the recent post process bookkeeping object */
trait RecentArticlePostProcessImplicits extends WordStormMongoConstants {

  implicit object RecentArticlePostProcessWriter
      extends BSONDocumentWriter[RecentArticlePostProcess] {

    override def write(rapp: RecentArticlePostProcess): BSONDocument = BSONDocument(
      RecentPostProcessConstants.StartTimeString -> BSONDateTime(rapp.startTime),
      RecentPostProcessConstants.StrategyString -> BSONInteger(rapp.strategy)
    )
  }

  implicit object RecentArticlePostProcessReader
      extends BSONDocumentReader[RecentArticlePostProcess] {

    override def read(doc: BSONDocument): RecentArticlePostProcess = {
      val startTime = doc.getAs[Date](RecentPostProcessConstants.StartTimeString).get.getTime
      val strategy = doc.getAs[Int](RecentPostProcessConstants.StrategyString).get

      RecentArticlePostProcess(
        startTime = startTime,
        strategy = strategy
      )
    }
  }

}
