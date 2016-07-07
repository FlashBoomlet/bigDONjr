package com.flashboomlet.charts

import java.text.SimpleDateFormat
import java.util.Locale

import breeze.plot.Figure
import breeze.plot.plot
import com.flashboomlet.interpolation.Interpolation.funcToArray
import com.flashboomlet.interpolation.Interpolation.interpolateData
import com.typesafe.scalalogging.LazyLogging
import com.flashboomlet.models.PostProcessData

import scala.util.Try

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
    */
  def contentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)
    entities.foreach { entity =>
      val t = entity._2.map(_.publishStartDate.toDouble)
      val count = entity._2.map(_.contentCount.toDouble)
      p += plot(t, count, '.', name = entity._1, shapes = true)
    }

    p.xlabel = "Time (5 Minute Groups)"
    p.ylabel = "Tweet Count"
    p.legend = true
    p.title = "Content Count"

    Try {
      f.saveas(fileLocation + "ContentCount_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: ContentCount, at $time")
    )
  }

  /**
    * The content count by the time of day
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def contentCountByTOD(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)
    entities.foreach { entity =>
      p += plot(entity._2.map(_.minute),
        entity._2.map(_.contentCount), '.', name = entity._1)

      val listOfYs = entity._2.groupBy(_.minute).map(g =>
        (g._1, g._2.map(i => i.contentCount))).toList
      val uniques = listOfYs.map(i =>
        (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)
      Try {
        val func = funcToArray(interpolateData(uniques.map(_._1),
          uniques.map(_._2), 5), uniques.map(_._1))
        p += plot(uniques.map(_._1), func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))

    }

    p.xlabel = "Time Of Day"
    p.ylabel = "Tweet Count"
    p.legend = true
    p.title = "Content Count by Time of Day"

    Try {
      f.saveas(fileLocation + "ContentCountByTOD_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: ContentCountByTOD, at $time")
    )
  }

  /**
    * The content count per entity
    *
    * @param data the list of data to be evaluated
    * @param entity the entity to be evaluated
    * @param fileLocation the location to try to output the file to
    */
  def entityCount(
    data: List[PostProcessData],
    entity: String,
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)
    val t = data.map(_.publishStartDate.toDouble)

    val contentCount = data.map(_.contentCount.toDouble)
    p += plot(t, contentCount, '.', name = "Content Count")

    val sentenceCount = data.map(_.totalSentences.toDouble)
    p += plot(t, sentenceCount, '.', name = "Sentence Count")

    val wordCount = data.map(_.totalWords.toDouble)
    p += plot(t, wordCount, '.', name = "Word Count")

    val authorCount = data.map(_.uniqueAuthors.toDouble)
    p += plot(t, authorCount, '.', name = "Author Count")

    p.title = "Content Counts for: " + entity
    p.xlabel = "Time (5 Minute Groups)"
    p.ylabel = "Number of Occurrences"
    p.legend = true

    Try {
      f.saveas(fileLocation + "EntityCount_" + entity + "_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: EntityCount, at $time")
    )
  }

  /**
    * The content count by day for an entity
    *
    * @param data the list of data to be evaluated
    * @param entity the entity to be evaluated
    * @param fileLocation the location to try to output the file to
    */
  def entityCountByDay(
    data: List[PostProcessData],
    entity: String,
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

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
    val contentCount = day.map(_._2).toList
    p += plot(t, contentCount, '.', name = "Content Count")

    val sentenceCount = day.map(_._3).toList
    p += plot(t, sentenceCount, '.', name = "Sentence Count")

    val wordCount = day.map(_._4).toList
    p += plot(t, wordCount, '.', name = "Word Count")

    val authorCount = day.map(_._5).toList
    p += plot(t, authorCount, '.', name = "Author Count")


    p.title = "Content Counts by Day for: " + entity
    p.xlabel = "Day"
    p.ylabel = "Number of Occurrences"
    p.legend = true

    Try {
      f.saveas(fileLocation + "EntityCountByDay_" + entity + "_" + time + ".png")
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: EntityCountByDay, at $time")
    )
  }
}
