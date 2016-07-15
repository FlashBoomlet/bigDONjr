package com.flashboomlet.charts

import com.flashboomlet.interpolation.Interpolation.interpolateData
import com.flashboomlet.interpolation.Interpolation.funcToArray
import com.flashboomlet.models.PostProcessData
import com.typesafe.scalalogging.LazyLogging
import com.flashboomlet.charts.GenericChartPlotter.chartPointPlot

/**
  * Average Sentiment is to view the data by the average sentiment at a given time interval
  */
object AverageSentiment extends LazyLogging with ChartConstants {

  /**
    * Average Sentiment shows the average sentiment at a given 5 minute interval
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def writeAverageSentimentGraph(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.publishStartDate.toDouble, p.averageSentiment))
        ),
        ChartPoint(
          seriesName = e._1 + " - Interpolation",
          plotType = '-',
          data = (e._2.map(_.publishStartDate.toDouble) zip funcToArray(
            interpolateData(e._2.zipWithIndex.map(_._2.toDouble),
            e._2.map(_.averageSentiment), 7), e._2.zipWithIndex.map(_._2.toDouble))).sortBy(_._1)
        )
      )
    }
    chartPointPlot(
      chartPoints,
      "Average Sentiment",
      "Time (5 Minute Groups)",
      "Sentiment",
      fileLocation,
      time
    )
  }

  /**
    * Average Sentiment shows the average sentiment at a given a time of day
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def averageSentimentByTOD(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.minute))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val listOfYs = e._2.groupBy(_.minute).map(g =>
        (g._1, g._2.map(i => i.averageSentiment))).toList
      val uniques = listOfYs.map(i => (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)

      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.minute.toDouble, p.averageSentiment))
        ),
        ChartPoint(
          seriesName = e._1 + " - Interpolation",
          plotType = '-',
          data = (uniques.map(_._1) zip funcToArray(interpolateData(uniques.map(_._1),
            uniques.map(_._2), 6), uniques.map(_._1))).sortBy(_._1)
        )
      )
    }
    chartPointPlot(
      chartPoints,
      "Average Sentiment By Time of Day",
      "Time (5 Minute Groups)",
      "Sentiment",
      fileLocation,
      time
    )
  }
}
