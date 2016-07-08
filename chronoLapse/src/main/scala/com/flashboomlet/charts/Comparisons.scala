package com.flashboomlet.charts

import breeze.plot.Figure
import breeze.plot.plot
import com.flashboomlet.interpolation.Interpolation.funcToArray
import com.flashboomlet.interpolation.Interpolation.interpolateData
import com.flashboomlet.models.PostProcessData
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
  * Comparisons shows the comparisions between data
  */
object Comparisons extends LazyLogging with ChartConstants {

  /**
    * Sentiment Vs Content Count shows the average sentiment at a given content count level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def sentimentVsContentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val y = entity._2.map(_.averageSentiment)
      val listOfYs = entity._2.groupBy(_.contentCount).map(g =>
        (g._1, g._2.map(i => i.averageSentiment))).toList
      val uniques = listOfYs.map(i =>
        (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)
      val x = entity._2.map(_.contentCount.toDouble)
      p += plot(
        x,
        y,
        '.',
        name = entity._1)
      Try {
        val func = funcToArray(interpolateData(uniques.map(_._1),
          uniques.map(_._2), 2), uniques.map(_._1))
        p += plot(uniques.map(_._1), func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))

    }
    p.xlabel = "Content Count"
    p.ylabel = "Sentiment"
    p.legend = true
    p.title = "Sentiment Vs. Content Count"

    Try {
      f.saveas(fileLocation + "SentimentVsContentCount_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: SentimentVsContentCount, at $time")
    )
  }

  /**
    * Sentiment Vs Word Count shows the average sentiment at a given word count level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def sentimentVsWordCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val y = entity._2.map(_.averageSentiment)
      val x = entity._2.map(_.totalWords.toDouble)
      p += plot(
        x,
        y,
        '.',
        name = entity._1)

      val listOfYs = entity._2.groupBy(_.totalWords).map(g =>
        (g._1, g._2.map(i => i.averageSentiment))).toList
      val uniques = listOfYs.map(i =>
        (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)

      Try {
        val func = funcToArray(interpolateData(uniques.map(_._1),
          uniques.map(_._2), 2), uniques.map(_._1))
        p += plot(uniques.map(_._1), func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))
    }
    p.xlabel = "Content Count"
    p.ylabel = "Sentiment"
    p.legend = true
    p.title = "Sentiment Vs. Word Count"

    Try {
      f.saveas(fileLocation + "SentimentVsWordCount_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: SentimentVsWordCount, at $time")
    )
  }

  /**
    * Sentiment Vs Percent Negative shows the sentiment at a given percent negative level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def sentimentVsPercentNegative(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val x = entity._2.map(_.averageSentiment)
      val y = entity._2.map(_.percentNegativeSentiment)
      p += plot(
        x,
        y,
        '.',
        name = entity._1)
    }
    p.xlabel = "Sentiment"
    p.ylabel = "Negative Percentage"
    p.legend = true
    p.title = "Sentiment Vs. Negative Percentage"

    Try {
      f.saveas(fileLocation + "SentimentVsNegativePercentage_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: SentimentVsNegativePercentage, at $time")
    )
  }

  /**
    * Sentiment Vs Percent Positive shows the sentiment at a given percent positive level
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def sentimentVsPercentPositive(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val x = entity._2.map(_.averageSentiment)
      val y = entity._2.map(_.percentPositiveSentiment)
      p += plot(
        x,
        y,
        '.',
        name = entity._1)
    }
    p.xlabel = "Sentiment"
    p.ylabel = "Positive Percentage"
    p.legend = true
    p.title = "Sentiment Vs. Positive Percentage"

    Try {
      f.saveas(fileLocation + "SentimentVsPositivePercentage_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: SentimentVsPositivePercentage, at $time")
    )
  }

  /**
    * Percent Positive vs Unique Author Count shows the average percent positive level vs a given
    * level of authors
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def positivePercentageVsUniqueAuthor(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val x = entity._2.map(_.uniqueAuthors.toDouble)
      val y = entity._2.map(_.percentPositiveSentiment)
      val listOfYs = entity._2.groupBy(_.uniqueAuthors).map(g =>
        (
          g._1.toDouble,
          g._2.map(i => i.percentPositiveSentiment.toFloat).sum,
          g._2.map(i => i.percentNegativeSentiment).length.toFloat
        )
      ).toList
      val uniques = listOfYs.map(i => (i._1, (i._2 / i._3).toDouble )).sortBy(_._1).reverse
      p += plot(
        x,
        y,
        '.',
        name = entity._1)

      Try {
        val func = funcToArray(interpolateData(uniques.map(_._1),
          uniques.map(_._2), 2), uniques.map(_._1))
        p += plot(uniques.map(_._1), func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))
    }
    p.xlabel = "Unique Author Count"
    p.ylabel = "Positive Percentage"
    p.legend = true
    p.title = "Positive Percentage Vs. Unique Author Count"

    Try {
      f.saveas(fileLocation + "PositivePercentageVsUniqueAuthorCount_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: PositivePercentageVsUniqueAuthorCount, at $time")
    )
  }

  /**
    * Percent Positive vs Content Count shows the average percent positive level vs a given
    * level of content
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def positivePercentageVsContentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val x = entity._2.map(_.contentCount.toDouble)
      val y = entity._2.map(_.percentPositiveSentiment)
      val listOfYs = entity._2.groupBy(_.contentCount).map(g =>
        (g._1, g._2.map(i => i.percentPositiveSentiment))).toList
      val uniques = listOfYs.map(i => (i._1.toDouble, i._2.sum / i._2.size.toDouble)).sortBy(_._1)
      p += plot(
        x,
        y,
        '.',
        name = entity._1)

      Try {
        val func = funcToArray(interpolateData(uniques.map(_._1), uniques.map(_._2), 2),
          uniques.map(_._1))
        p += plot(uniques.map(_._1), func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))
    }
    p.xlabel = "Positive Percentage"
    p.ylabel = "Content Count"
    p.legend = true
    p.title = "Positive Percentage Vs. Content Count"

    Try {
      f.saveas(fileLocation + "PositivePercentageVsContentCount_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: PositivePercentageVsContentCount, at $time")
    )
  }

  /**
    * Author Count vs Content Count plots ever data point in relation to the amount of authors to
    * the amount of content
    *
    * @param entities the list of data to be evaluated by entity
    * @param fileLocation the location to try to output the file to
    */
  def authorCountVsContentCount(
    entities: List[(String, List[PostProcessData])],
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)

    entities.foreach { entity =>
      val x = entity._2.map(_.uniqueAuthors.toDouble)
      val y = entity._2.map(_.contentCount.toDouble)
      p += plot(
        x,
        y,
        '.',
        name = entity._1)

      Try {
        val func = funcToArray(interpolateData(x, y, 1), x)
        p += plot(x, func, '-', name = "Interpolation for: " + entity._1)
      }.getOrElse( logger.info(s"Failed to Interpolate data at $time"))
    }
    p.xlabel = "Author Count"
    p.ylabel = "Content Count"
    p.legend = true
    p.title = "Author Count Vs. Content Count"

    Try {
      f.saveas(fileLocation + "AuthorCountVsContentCount_" + time + ".png" )
    }.getOrElse(
      logger.info(s"Failed to Create Chart for: AuthorCountVsContentCount, at $time")
    )
  }
}
