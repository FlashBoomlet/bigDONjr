package com.flashboomlet.predicting

import com.flashboomlet.data.models.PollsterDataPoint

/**
  * Instance Based Prediction
  *
  * Created on 07/09/2016
  */
object InstanceBasedPrediction {

  final val sizeOfPattern = 16

  final val similarityThreshold = 75.0

  final val fullPercentage = 100.0

  def predictValues(pollsterDataPoint: List[PollsterDataPoint]): List[PollsterPattern] = {

    val dateRange = Range(sizeOfPattern, pollsterDataPoint.length - sizeOfPattern, 1).toList
    val formattedData = formatPollData(pollsterDataPoint)
    val patternizedData = patternizeData(dateRange, formattedData)
    predictNextPointValidation(patternizedData)
  }

  /**
    * Predict Next Point Validation is a function that predicts the next point from past data
    * and validates it
    *
    * @param patternizedData the patternized data from the pollster patterns
    * @return the patternized data with predictions and error in predictions
    */
  private def predictNextPointValidation(
    patternizedData: List[PollsterPattern]): List[PollsterPattern] = {

    patternizedData.drop(1).map { day =>
      val pastPatterns = patternizedData.filter(p => p.endIndex < day.endIndex)
      PollsterPattern(
        startIndex = day.startIndex,
        endIndex = day.endIndex,
        patternSize = day.patternSize,
        clinton = findPastPatternsAndPredictValidation(day.clinton,
          pastPatterns.map(_.clinton)),
        trump = findPastPatternsAndPredictValidation(day.trump,
          pastPatterns.map(_.trump)),
        other = findPastPatternsAndPredictValidation(day.other,
          pastPatterns.map(_.other)),
        undecided = findPastPatternsAndPredictValidation(day.undecided,
          pastPatterns.map(_.undecided))
      )
    }
  }

  /**
    * Find Past Patterns And Predict Validation finds the next day prediction and validates it off
    * of the past data points.
    *
    * @param data the current day pattern
    * @param patternizedData the patterns of data to search through
    * @return an update object to include the prediction value and the percent error of it
    */
  private def findPastPatternsAndPredictValidation(
    data: EntityPattern,
    patternizedData: List[EntityPattern]): EntityPattern = {

    val current = data.predictionPattern.map(_.percentChange)
    val predictedPercentChange = findPastPatternsAndPredict(current, patternizedData)
    EntityPattern(
      entity = data.entity,
      // Percent Change between actual value and predicted value
      predictedError = percentChange(data.predictionChange, predictedPercentChange),
      StartValue = data.StartValue,
      predictionChange = data.predictionChange,
      predictedChange = predictedPercentChange,
      predictionPattern = data.predictionPattern,
      predictionPatternLength = data.predictionPatternLength
    )
  }

  /**
    * Find Past Patterns And Predict finds all past patterns that fall within the range of the
    * required similarity percentage/threshold and then predicts the next day value for the current
    * pattern off of that data.
    *
    * @param current the current day pattern
    * @param patternizedData the patterns of data to search through
    * @return a prediction for the next day
    */
  private def findPastPatternsAndPredict(
    current: List[Double],
    patternizedData: List[EntityPattern]): Double = {

    val validPatterns = patternizedData.filter(p => overallPercentSimilar(current,
      p.predictionPattern.map(_.percentChange)) >= similarityThreshold)
      .map(_.predictionChange)
    if (validPatterns.isEmpty) {
      current.last
    } else {
      validPatterns.sum / validPatterns.length
    }
  }

  /**
    * Patternize Data takes data and creates all of the patterns.
    *
    * @param dateRange a indexed list that corresponds to dates
    * @param formattedData the formatted data from pollster
    * @return the patternized data
    */
  private def patternizeData(
    dateRange: List[Int],
    formattedData: List[PollsterEntityData]): List[PollsterPattern] = {

    val patternSize = sizeOfPattern - 1
    dateRange.map { i =>
      PollsterPattern(
        startIndex = i,
        endIndex = i + sizeOfPattern,
        patternSize = patternSize,
        clinton = ToEntityPattern("Clinton", i, i + patternSize, formattedData),
        trump = ToEntityPattern("Trump", i, i + patternSize, formattedData),
        other = ToEntityPattern("Other", i, i + patternSize, formattedData),
        undecided = ToEntityPattern("Undecided", i, i + patternSize, formattedData)
      )
    }
  }

  /**
    * Formats the pollster data to be a list of data points per entity.
    *
    * @param data the raw pollster data points
    * @return the formatted pollster data points
    */
  private def formatPollData(data: List[PollsterDataPoint]): List[PollsterEntityData] = {
    val trumpPoll = PollsterEntityData("Trump",
      data.map(s => EntityData(s.date, s.trump)).sortBy(_.time))
    val clintonPoll = PollsterEntityData("Clinton",
      data.map(s => EntityData(s.date, s.clinton)).sortBy(_.time))
    val otherPoll = PollsterEntityData("Other",
      data.map(s => EntityData(s.date, s.other)).sortBy(_.time))
    val undecidedPoll = PollsterEntityData("Undecided",
      data.map(s => EntityData(s.date, s.undecided)).sortBy(_.time))
    List(trumpPoll, clintonPoll, otherPoll, undecidedPoll)
  }

  /**
    * To Entity Pattern converts Pollster Entity Data to an Entity Pattern
    *
    * @param entity the entity for the pattern
    * @param startIndex the start index for the pattern
    * @param endIndex the end index for the pattern
    * @param data the data to create a pattern from
    * @return the entity pattern
    */
  private def ToEntityPattern(
    entity: String,
    startIndex: Int,
    endIndex: Int,
    data: List[PollsterEntityData]): EntityPattern = {

    val zippedData = data.filter(p => p.entity == entity).flatMap(_.data.zipWithIndex)
    val entityData = zippedData.filter(p => p._2 >= startIndex && p._2 < endIndex).map(_._1).sortBy(_.time).reverse
    val len = entityData.length
    val start = entityData.head.percentage
    EntityPattern(
      entity = entity,
      predictedError = 1D,
      StartValue = start,
      predictionChange = percentChange(start, entityData.last.percentage),
      predictedChange = 0D,
      predictionPattern = zippedData.take(sizeOfPattern - 1).map(data =>
        PatternPoint(
          time = startIndex + data._2,
          percentChange = percentChange(start, data._1.percentage)
        )
      ),
      predictionPatternLength = len
    )
  }

  /**
    * Percent Change is a helper function to determine the percent change between two variables
    *
    * @param initial the initial data point
    * @param current the current data point
    * @return the percent change between the initial and the current data points
    */
  private def percentChange(initial: Double, current: Double): Double = {
    if (initial != 0) {
      ((current - initial) / Math.abs(initial)) * 100.0
    } else {
      0
    }
  }

  /**
    * Defines how similar two data points are.
    *
    * @param data the actual data
    * @param comparison the point to compare against
    * @return the percent of similarity between two points
    */
  private def percentSimilar(data: Double, comparison: Double): Double = {
    fullPercentage - Math.abs(percentChange(data, comparison))
  }

  /**
    * Overall percent similar is a helper function to determine how similar two sets of data are.
    *
    * NOTE: Order does in fact matter
    *
    * @param data the current data pattern
    * @param pattenData the data pattern to compare against
    * @return the overall percent similar that two lists of doubles are
    */
  private def overallPercentSimilar(data: List[Double], pattenData: List[Double]): Double = {
    val similarityArray = (data zip pattenData).map{ case (d, pd) =>
      percentSimilar(d, pd)
    }
    similarityArray.sum / similarityArray.length

  }
}
