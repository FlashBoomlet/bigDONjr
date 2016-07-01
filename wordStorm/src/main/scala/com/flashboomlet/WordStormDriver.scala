package com.flashboomlet

import akka.actor.ActorSystem
import akka.actor.Props
import com.flashboomlet.actors.AkkaConstants
import com.flashboomlet.actors.ArticlePostProcessTickActor
import com.flashboomlet.actors.PostProcessAkkaConstants
import com.flashboomlet.actors.TweetPostProcessTickActor
import com.flashboomlet.io.DatabaseController
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
  * Driver for the wordstorm project
  */
object WordStormDriver extends LazyLogging {

  val databaseController = DatabaseController()

  val system = ActorSystem("postprocessorsystem")

  /**
    * Entry point to the program.
 *
    * @param args Command-line arguments
    */
  def main(args: Array[String]): Unit = {
    configureScheduler()
  }


  /**
    * Configures and schedules all of the tick actors.
    */
  def configureScheduler(): Unit = {
    Try {
      import system.dispatcher // scalastyle:ignore import.grouping

      val articlePostProcessActor = system.actorOf(Props(classOf[ArticlePostProcessTickActor]))
      val tweetPostProcessActor = system.actorOf(Props(classOf[TweetPostProcessTickActor]))

      system.scheduler.schedule(
        PostProcessAkkaConstants.InitialDelay,
        PostProcessAkkaConstants.OneDayTickLength,
        articlePostProcessActor,
        AkkaConstants.Tick)

      system.scheduler.schedule(
        PostProcessAkkaConstants.InitialDelay,
        PostProcessAkkaConstants.OneDayTickLength,
        tweetPostProcessActor,
        AkkaConstants.Tick)
    }.getOrElse(logger.error("Failed to configure scheduler."))
  }
}
