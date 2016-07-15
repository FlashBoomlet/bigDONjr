package com.flashboomlet.charts

import breeze.plot.Figure
import breeze.plot.plot
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

/**
  * Generic Chart Plotter is a generic plotter of data.
  */
object GenericChartPlotter extends LazyLogging with ChartConstants {

  /**
    * Chart Point Plot is a generic function to plot
    *
    * @param rawData the chart points
    * @param title the title of the chart
    * @param xLabel the x label of the chart
    * @param yLabel the y label of the chart
    * @param fileLocation the file location to output the file to. ("" = no output)
    * @param time the time that the chart is produced off of
    */
  def chartPointPlot(
    rawData: List[ChartPoint],
    title: String,
    xLabel: String,
    yLabel: String,
    fileLocation: String,
    time: Long): Unit = {

    val f = Figure()
    f.height = chartHeight
    f.width = chartWidth
    val p = f.subplot(1, 1, 0)
    rawData.foreach { data =>
      val name = data.seriesName
      val entity = data.data.sortBy(_._1.toDouble).reverse
      val x = entity.map(_._1.toDouble)
      val y = entity.map(_._2)
      p += plot(x, y, data.plotType, name = name)
    }
    p.xlabel = xLabel
    p.ylabel = yLabel
    p.legend = true
    p.title = title
    if (fileLocation != "")
    {
      Try {
        f.saveas(fileLocation + title.replace(' ', '_') + "_" + time + ".png" )
      }.getOrElse(
        logger.info(s"Failed to Create Chart for: " + title + s", at $time "
        +s"at location: $fileLocation")
      )
    }
  }

}
