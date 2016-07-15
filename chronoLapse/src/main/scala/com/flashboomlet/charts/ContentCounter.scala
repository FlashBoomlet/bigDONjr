package com.flashboomlet.charts

import java.text.SimpleDateFormat
import java.util.Locale

import com.flashboomlet.charts.GenericChartPlotter.chartPointPlot
import com.flashboomlet.interpolation.Interpolation.funcToArray
import com.flashboomlet.interpolation.Interpolation.interpolateData
import com.typesafe.scalalogging.LazyLogging
import com.flashboomlet.models.PostProcessData

/**
  * Content Count is a way to view data by content
  */
object ContentCounter extends LazyLogging with ChartConstants {

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

  /**
    * Content Count is a way to view the content count at a given time interval
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def contentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).map{ e =>
      ChartPoint(
        seriesName = e._1,
        plotType = '.',
        data = e._2.map(p => (p.publishStartDate.toDouble, p.contentCount.toDouble))
      )
    }
    chartPointPlot(
      chartPoints,
      "Content Count",
      "Time (5 Minute Groups)",
      "Content Count",
      fileLocation,
      time
    )
  }

  /**
    * The content count by the time of day
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def contentCountByTOD(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val listOfYs = e._2.groupBy(_.minute).map(g =>
        (g._1, g._2.map(i => i.contentCount))).toList
      val uniques = listOfYs.map(i =>
        (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)

      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.minute.toDouble, p.contentCount.toDouble))
        ),
        ChartPoint(
          seriesName = e._1 + " - Interpolation",
          plotType = '-',
          data = (uniques.map(_._1) zip funcToArray(interpolateData(uniques.map(_._1),
            uniques.map(_._2), 5), uniques.map(_._1))).sortBy(_._1)
        )
      )
    }
    chartPointPlot(
      chartPoints,
      "Content Count by Time of Day",
      "Time Of Day",
      "Content Count",
      fileLocation,
      time
    )
  }

  /**
    * The content count per entity
    *
    * @param data the list of data to be evaluated
    * @param entity the entity to be evaluated
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def entityCount(
    data: List[PostProcessData],
    entity: String,
    fileLocation: String,
    time: Long): Unit = {

    val t = data.map(_.publishStartDate.toDouble)
    val contentCount = ChartPoint("Content Count", '.',
      (t zip data.map(_.contentCount.toDouble)).sortBy(_._1))
    val sentenceCount = ChartPoint("Sentence Count", '.',
      (t zip data.map(_.totalSentences.toDouble)).sortBy(_._1))
    val wordCount = ChartPoint("Word Count", '.',
      (t zip data.map(_.totalWords.toDouble)).sortBy(_._1))
    val authorCount = ChartPoint("Author Count", '.',
      (t zip data.map(_.uniqueAuthors.toDouble)).sortBy(_._1))
    val chartPoints = List(contentCount, sentenceCount, wordCount, authorCount)
    chartPointPlot(
      chartPoints,
      "Content Counts for: " + entity,
      "Time (5 Minute Groups)",
      "Number of Occurrences",
      fileLocation,
      time
    )
  }

  /**
    * The content count by day for an entity
    *
    * @param data the list of data to be evaluated
    * @param entity the entity to be evaluated
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def entityCountByDay(
    data: List[PostProcessData],
    entity: String,
    fileLocation: String,
    time: Long): Unit = {

    val day = data.groupBy(s => dateFormat.format(s.publishStartDate)).map { d =>
      (
        d._1.toDouble,
        d._2.map(_.contentCount.toDouble).sum,
        d._2.map(_.totalSentences.toDouble).sum,
        d._2.map(_.totalWords.toDouble).sum,
        d._2.map(_.uniqueAuthors.toDouble).sum
      )
    }
    val t = day.map(_._1).toList
    val contentCount = ChartPoint("Content Count", '.',
      (t zip day.map(_._2).toList).sortBy(_._1))
    val sentenceCount = ChartPoint("Sentence Count", '.',
      (t zip day.map(_._3).toList).sortBy(_._1))
    val wordCount = ChartPoint("Word Count", '.',
      (t zip day.map(_._4).toList).sortBy(_._1))
    val authorCount = ChartPoint("Author Count", '.',
      (t zip day.map(_._5).toList).sortBy(_._1))
    val chartPoints = List(contentCount, sentenceCount, wordCount, authorCount)
    chartPointPlot(
      chartPoints,
      "Content Counts by Day for: " + entity,
      "Day",
      "Number of Occurrences",
      fileLocation,
      time
    )
  }
}
