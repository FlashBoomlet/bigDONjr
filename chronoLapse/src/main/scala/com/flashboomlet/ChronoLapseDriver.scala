package com.flashboomlet

import java.io.File
import java.util.Date

import com.flashboomlet.charts.AverageSentiment.writeAverageSentimentGraph
import com.flashboomlet.charts.AverageSentiment.averageSentimentByTOD
import com.flashboomlet.charts.ContentCounter.contentCount
import com.flashboomlet.charts.ContentCounter.contentCountByTOD
import com.flashboomlet.charts.ContentCounter.entityCount
import com.flashboomlet.charts.Comparisons.sentimentVsContentCount
import com.flashboomlet.charts.Comparisons.sentimentVsPercentNegative
import com.flashboomlet.charts.Comparisons.sentimentVsPercentPositive
import com.flashboomlet.charts.Comparisons.sentimentVsWordCount
import com.flashboomlet.charts.Comparisons.positivePercentageVsUniqueAuthor
import com.flashboomlet.charts.Comparisons.positivePercentageVsContentCount
import com.flashboomlet.charts.Comparisons.authorCountVsContentCount
import com.flashboomlet.data.EntityFactory
import com.flashboomlet.io.DatabaseController
import com.flashboomlet.models.PostProcessData

object ChronoLapseDriver {

  val databaseController: DatabaseController = DatabaseController()

  final val fileLocation = "Graphs/"

  final val entities = List("Clinton", "Trump")

  def main(args: Array[String]): Unit = {
    val beginning: Long = 1466900000000L
    val currentTime: Long = new Date().getTime
    val tweets = entities.map(e =>
      (
      e,
      databaseController.getTweetPostProcesses(e, beginning, currentTime)
      )
    )

    val nytArticles = entities.map(e =>
      (
      e,
      databaseController.getArticlePostProcesses(e, beginning, currentTime)
      )
    )

    writeCharts("Twitter", currentTime, tweets)
    writeCharts("NYTArticles", currentTime,  nytArticles)
  }

  /**
    * Helper function to aid in printing out graphs of data.
    *
    * @param source the source of the data
    * @param data a list of an entity with its corresponding post process data
    */
  def writeCharts(
    source: String,
    time: Long,
    data: List[(String, List[PostProcessData])]): Unit = {

    val location = fileLocation + source + "_" + time + "/"
    val dir = new File(location)
    dir.mkdir()

    /*
     * Haven't Decided if this will be useful right now... could be though...
    entities.foreach(entity =>
      entityCount(data.filter(s => s._1 == entity)
        .flatMap(s => s._2), entity + source, location, time)
    )
    */
    contentCount(data, location, time)
    contentCountByTOD(data, location, time)
    writeAverageSentimentGraph(data, location, time)
    averageSentimentByTOD(data, location, time)
    sentimentVsContentCount(data, location, time)
    sentimentVsWordCount(data, location, time)
    sentimentVsPercentPositive(data, location, time)
    sentimentVsPercentNegative(data, location, time)
    positivePercentageVsUniqueAuthor(data, location, time)
    // positivePercentageVsContentCount(data, location, time) The two correlate extremely close
    authorCountVsContentCount(data, location, time)
  }
}
