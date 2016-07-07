package com.flashboomlet.charts

import breeze.plot.Figure
import breeze.plot.plot
import com.flashboomlet.interpolation.Interpolation.interpolateData
import com.flashboomlet.interpolation.Interpolation.funcToArray
import com.flashboomlet.models.PostProcessData
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
  * Average Sentiment is to view the data by the average sentiment at a given time interval
  */
object AverageSentiment extends LazyLogging with ChartConstants {

  /**
    * Average Sentiment shows the average sentiment at a given 5 minute interval
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def writeAverageSentimentGraph(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)
    entities.foreach { e =>
      val entity = e._2
      val t = entity.map(_.publishStartDate.toDouble)
      val tInterpolation = t.zipWithIndex.map(_._2.toDouble)
      val sent = entity.map(_.averageSentiment)
      p += plot(t, sent, '.', name = e._1)
      // Watch the Degree. Could run into overflow error
      Try{
        val func = funcToArray(interpolateData(tInterpolation, sent, 5), tInterpolation)
        p += plot(t, func, '-', name = "Interpolation for: " + e._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))
    }

    p.xlabel = "Time (5 Minute Groups)"
    p.ylabel = "Sentiment"
    p.legend = true
    p.title = "Average Sentiment"

    Try {
      f.saveas(fileLocation + "AverageSentiment_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: AverageSentiment, at $time")
    )
  }

  /**
    * Average Sentiment shows the average sentiment at a given a time of day
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def averageSentimentByTOD(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)
    entities.foreach { entity =>
      p += plot(entity._2.map(_.minute.toDouble),
        entity._2.map(_.averageSentiment), '.', name = entity._1)

      val listOfYs = entity._2.groupBy(_.minute).map(g =>
        (g._1, g._2.map(i => i.averageSentiment))).toList
      val uniques = listOfYs.map(i => (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)

      Try{
        val func = funcToArray(interpolateData(uniques.map(_._1),
          uniques.map(_._2), 5), uniques.map(_._1))
        p += plot(uniques.map(_._1), func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))
    }

    p.xlabel = "Time (5 Minute Groups)"
    p.ylabel = "Sentiment"
    p.legend = true
    p.title = "Average Sentiment By Time of Day"

    Try {
      f.saveas(fileLocation + "AverageSentimentByTOD_" + time + ".png")
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: AverageSentimentByTOD, at $time")
    )
  }
}
