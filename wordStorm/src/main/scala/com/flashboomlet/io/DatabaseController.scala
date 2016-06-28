package com.flashboomlet.io

import com.flashboomlet.db.MongoDatabaseDriver
import com.flashboomlet.db.implicits.MongoImplicits
import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection

/**
  * Contains behavior for database functionality
  */
class DatabaseController
    extends WordStormMongoConstants
    with LazyLogging
    with MongoImplicits
    with ArticlePostProcessDataImplicits
    with TweetPostProcessDataImplicits {

  /** Instance of pre-configured database driver. */
  val databaseDriver = MongoDatabaseDriver()

  /** Database collection for article post-process data */
  val articlePostProcessDatasCollection: BSONCollection = databaseDriver
    .db(ArticlePostProcessDatasCollectionString)

  /** Database collection for tweet post process data */
  val tweetPostProcessDatasCollection: BSONCollection = databaseDriver
    .db(TweetPostProcessDatasCollectionString)

}

/**
  * Companion object for the Databse controller
  */
object DatabaseController {

  /** Factory method for the Database Controller */
  def apply(): DatabaseController = new DatabaseController
}
