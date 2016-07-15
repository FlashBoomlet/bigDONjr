package com.flashboomlet.charts

import com.flashboomlet.charts.GenericChartPlotter.chartPointPlot
import com.flashboomlet.interpolation.Interpolation.funcToArray
import com.flashboomlet.interpolation.Interpolation.interpolateData
import com.flashboomlet.models.PostProcessData
import com.typesafe.scalalogging.LazyLogging

/**
  * Comparisons shows the comparisons between data
  */
object Comparisons extends LazyLogging with ChartConstants {

  /**
    * Sentiment Vs Content Count shows the average sentiment at a given content count level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def sentimentVsContentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val listOfYs = e._2.groupBy(_.contentCount).map(g =>
        (g._1, g._2.map(i => i.averageSentiment))).toList
      val uniques = listOfYs.map(i =>
        (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)
      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.contentCount.toDouble, p.averageSentiment))
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
      "Sentiment Vs Content Count",
      "Content Count",
      "Sentiment",
      fileLocation,
      time
    )
  }

  /**
    * Sentiment Vs Word Count shows the average sentiment at a given word count level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def sentimentVsWordCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val listOfYs = e._2.groupBy(_.totalWords).map(g =>
        (g._1, g._2.map(i => i.averageSentiment))).toList
      val uniques = listOfYs.map(i =>
        (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)
      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.totalWords.toDouble, p.averageSentiment))
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
      "Sentiment Vs Word Count",
      "Word Count",
      "Sentiment",
      fileLocation,
      time
    )
  }

  /**
    * Sentiment Vs Percent Negative shows the sentiment at a given percent negative level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def sentimentVsPercentNegative(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).map{ e =>
      ChartPoint(
        seriesName = e._1,
        plotType = '.',
        data = e._2.map(p => (p.averageSentiment, p.percentNegativeSentiment))
      )
    }
    chartPointPlot(
      chartPoints,
      "Sentiment Vs Negative Percentage",
      "Sentiment",
      "Negative Percentage",
      fileLocation,
      time
    )
  }

  /**
    * Sentiment Vs Percent Positive shows the sentiment at a given percent positive level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def sentimentVsPercentPositive(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).map{ e =>
      ChartPoint(
        seriesName = e._1,
        plotType = '.',
        data = e._2.map(p => (p.averageSentiment, p.percentPositiveSentiment))
      )
    }
    chartPointPlot(
      chartPoints,
      "Sentiment Vs Positive Percentage",
      "Sentiment",
      "Positive Percentage",
      fileLocation,
      time
    )
  }

  /**
    * Percent Positive vs Unique Author Count shows the average percent positive level vs a given
    * level of authors
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def positivePercentageVsUniqueAuthor(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val listOfYs = e._2.groupBy(_.uniqueAuthors).map(g =>
        (
        g._1.toDouble,
        g._2.map(i => i.percentPositiveSentiment.toFloat).sum,
        g._2.map(i => i.percentNegativeSentiment).length.toFloat
        )
      ).toList
      val uniques = listOfYs.map(i => (i._1, (i._2 / i._3).toDouble )).sortBy(_._1).reverse
      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.uniqueAuthors.toDouble, p.percentPositiveSentiment))
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
      "Positive Percentage Vs Unique Author Count",
      "Unique Author Count",
      "Positive Percentage",
      fileLocation,
      time
    )
  }

  /**
    * Percent Positive vs Content Count shows the average percent positive level vs a given
    * level of content
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def positivePercentageVsContentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val listOfYs = e._2.groupBy(_.contentCount).map(g =>
        (g._1, g._2.map(i => i.percentPositiveSentiment))).toList
      val uniques = listOfYs.map(i => (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)
      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.contentCount.toDouble, p.percentPositiveSentiment))
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
      "Positive Percentage Vs Content Count",
      "Content Count",
      "Positive Percentage",
      fileLocation,
      time
    )
  }

  /**
    * Author Count vs Content Count plots ever data point in relation to the amount of authors to
    * the amount of content
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    * @param time the time that the chart is generated off of
    */
  def authorCountVsContentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val chartPoints = entities.map( e => (e._1, e._2.sortBy(_.publishStartDate))).flatMap{ e =>
      // Series to aid in interpolating the data based on the grouping of the data
      val x = e._2.map(_.uniqueAuthors.toDouble)
      val y = e._2.map(_.contentCount.toDouble)
      List(
        ChartPoint(
          seriesName = e._1,
          plotType = '.',
          data = e._2.map(p => (p.uniqueAuthors.toDouble, p.contentCount.toDouble))
        ),
        ChartPoint(
          seriesName = e._1 + " - Interpolation",
          plotType = '-',
          data = (x zip funcToArray(interpolateData(x, y, 1), x)).sortBy(_._1)
        )
      )
    }
    chartPointPlot(
      chartPoints,
      "Author Count Vs Content Count",
      "Author Count",
      "Content Count",
      fileLocation,
      time
    )
  }
}
