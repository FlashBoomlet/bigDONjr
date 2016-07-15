package com.flashboomlet.charts

/**
  * Chart Point is a data structure for plotting data on charts
  * @param seriesName the name of the series
  * @param plotType the type of plot ('.', '+', or '-')
  * @param data the actual time series based data.
  */
case class ChartPoint(
  seriesName: String,
  plotType: Char,
  data: List[(Double, Double)]
)
