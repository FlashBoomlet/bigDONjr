package com.flashboomlet.predicting

import com.flashboomlet.charts.ChartPoint

/**
  * Pollster Pattern Conversion converts pollster pattern data to chart point data
  *
  * Very! Rough code that gets the job done for now. Only used to graph. To lazy to abstract.
  */
object PollsterPatternConversion {

  /**
    * Pollster Pattern To Chart Point converts pollster pattern based data to chart point data
    * @param data the pollster pattern based data
    * @return chart point data
    */
  def pollsterPatternToChartPoint(data: List[PollsterPattern]): List[ChartPoint] = {
    /*
     * Clinton Points
     */
    val clintonPoints = data.map { p =>
      (p.startIndex,
      p.clinton.entity,
      p.clinton.StartValue,
      p.clinton.entity + "- Predicted Value",
      p.clinton.StartValue * (1 + (p.clinton.predictedChange / 100.0))
      )
    }
    val regularClinton = ChartPoint(
      seriesName = clintonPoints.head._2, '-',
      clintonPoints.map(p => (p._1.toDouble, p._3)))
    val predictionClinton = ChartPoint(
      seriesName = clintonPoints.head._4, '-',
      clintonPoints.map(p => (p._1.toDouble, p._5)))

    /*
     * Trump Points
     */
    val trumpPoints = data.map { p =>
      (p.startIndex,
      p.trump.entity,
      p.trump.StartValue,
      p.trump.entity + "- Predicted Value",
      p.trump.StartValue * (1 + (p.trump.predictedChange / 100.0))
      )
    }
    val regularTrump = ChartPoint(
      seriesName = trumpPoints.head._2, '-',
      trumpPoints.map(p => (p._1.toDouble, p._3)))
    val predictionTrump = ChartPoint(
      seriesName = trumpPoints.head._4, '-',
      trumpPoints.map(p => (p._1.toDouble, p._5)))

    /*
     * Other Points
     */
    val otherPoints = data.map { p =>
      (p.startIndex,
      p.other.entity,
      p.other.StartValue,
      p.other.entity + "- Predicted Value",
      p.other.StartValue * (1 + (p.trump.predictedChange / 100.0))
      )
    }
    val regularOther = ChartPoint(
      seriesName = otherPoints.head._2, '-',
      otherPoints.map(p => (p._1.toDouble, p._3)))
    val predictionOther = ChartPoint(
      seriesName = otherPoints.head._4, '-',
      otherPoints.map(p => (p._1.toDouble, p._5)))

    /*
     * Undecided Points
     */
    val undecidedPoints = data.map { p =>
      (p.startIndex,
      p.undecided.entity,
      p.undecided.StartValue,
      p.undecided.entity + "- Predicted Value",
      p.undecided.StartValue * (1 + (p.trump.predictedChange / 100.0))
      )
    }
    val regularUndecided = ChartPoint(
      seriesName = undecidedPoints.head._2, '-',
      undecidedPoints.map(p => (p._1.toDouble, p._3)))
    val predictionUndecided = ChartPoint(
      seriesName = undecidedPoints.head._4, '-',
      undecidedPoints.map(p => (p._1.toDouble, p._5)))

    List(regularClinton, predictionClinton, regularTrump, predictionTrump, regularOther,
      predictionOther, regularUndecided, predictionUndecided)
  }
}
