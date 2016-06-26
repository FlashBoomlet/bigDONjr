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
    with MongoImplicits {

  /** Instance of pre-configured database driver. */
  val databaseDriver = MongoDatabaseDriver()

  /** Databse collection for pos-proccess data */
  val postProcessDatasCollection: BSONCollection = databaseDriver
    .db(PostProcessDatasCollectionString)

}

/**
  * Companion object for the Databse controller
  */
object DatabaseController {

  /** Factory method for the Database Controller */
  def apply(): DatabaseController = new DatabaseController
}
