package com.flashboomlet

import java.io.File
import java.util.Date

import com.flashboomlet.charts.AverageSentiment.writeAverageSentimentGraph
import com.flashboomlet.charts.AverageSentiment.averageSentimentByTOD
import com.flashboomlet.charts.ContentCounter.contentCount
import com.flashboomlet.charts.ContentCounter.contentCountByTOD
import com.flashboomlet.charts.Comparisons.sentimentVsContentCount
import com.flashboomlet.charts.Comparisons.sentimentVsPercentNegative
import com.flashboomlet.charts.Comparisons.sentimentVsPercentPositive
import com.flashboomlet.charts.Comparisons.sentimentVsWordCount
import com.flashboomlet.charts.Comparisons.positivePercentageVsUniqueAuthor
import com.flashboomlet.charts.Comparisons.authorCountVsContentCount
import com.flashboomlet.io.DatabaseController
import com.flashboomlet.models.PostProcessData
import com.flashboomlet.charts.GenericChartPlotter.chartPointPlot
import com.flashboomlet.predicting.PollsterPatternConversion

import com.flashboomlet.predicting.InstanceBasedPrediction

object ChronoLapseDriver {

  val databaseController: DatabaseController = DatabaseController()

  final val fileLocation = "Graphs/"

  final val entities = List("Clinton", "Trump")

  def main(args: Array[String]): Unit = {
    // A good begining that avoids the large gaps: 1467400000000L
    val beginning: Long = 1466900000000L
    val currentTime: Long = new Date().getTime

    databaseController.dumpRecentPostProcess
    val tweets = entities.map(e =>
      (
      e,
      databaseController.getTweetPostProcesses(e, beginning, currentTime).filter(p =>
        p.publishStartDate > beginning)
      )
    )
    val nytArticles = entities.map(e =>
      (
      e,
      databaseController.getArticlePostProcesses(e, beginning, currentTime).filter(p =>
        p.publishStartDate > beginning)
      )
    )
    val rawPollsterData = databaseController.getPollsterDataPointData()
    val evaluatedData = InstanceBasedPrediction.predictValues(rawPollsterData)

    val location = fileLocation + currentTime + "/"
    val dir = new File(location)
    dir.mkdir()

    chartPointPlot(PollsterPatternConversion.pollsterPatternToChartPoint(evaluatedData),
      "Pollster Data", "Time", "Percentage", location, currentTime)
    writeCharts("Twitter", currentTime, tweets, location)
    writeCharts("NYTArticles", currentTime,  nytArticles, location)
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
    data: List[(String, List[PostProcessData])],
    filePath: String): Unit = {

    val location = filePath + source + "/"
    val dir = new File(location)
    dir.mkdir()

    writeAverageSentimentGraph(data, location, time)

    contentCount(data, location, time)

    contentCountByTOD(data, location, time)
    averageSentimentByTOD(data, location, time)
    sentimentVsContentCount(data, location, time)
    sentimentVsWordCount(data, location, time)
    sentimentVsPercentPositive(data, location, time)
    sentimentVsPercentNegative(data, location, time)
    positivePercentageVsUniqueAuthor(data, location, time)
    authorCountVsContentCount(data, location, time)

  }
}
