package com.flashboomlet.predicting

/**
  * Pollster Pattern is a container holding Pollster Data Point
  *
  * Created by ttlynch on 7/9/16.
  *
  * @param startIndex the start index of the time series
  * @param endIndex the end index of the time series (Size: n)
  * @param patternSize the size of the pattern
  * @param clinton the entity pattern for clinton
  * @param trump the entity pattern for trump
  * @param other the entity pattern for other
  * @param undecided the entity pattern for undecided
  */
case class PollsterPattern(
  startIndex: Int,
  endIndex: Int,
  patternSize: Int,
  clinton: EntityPattern,
  trump: EntityPattern,
  other: EntityPattern,
  undecided: EntityPattern
)

/**
  * Entity Pattern is a container holding data on an entity
  *
  * Created by ttlynch on 7/9/16.
  *
  * @param entity the entity that corresponds to the data
  * @param predictedError the error in the prediction
  * @param StartValue the average value of the patterns raw percentage points
  * @param predictionChange the value of the prediction point in a % change
  * @param predictedChange the value of the prediction point in a % change
  * @param predictionPattern array of % change on past values
  * @param predictionPatternLength the length of the % change on past values list
  *
  * Hold prediction patterns for various linear interpolations
  */
case class EntityPattern(
  entity: String,
  predictedError: Double,
  StartValue: Double,
  predictionChange: Double,
  predictedChange: Double,
  predictionPattern: List[PatternPoint],
  predictionPatternLength: Int
)

/**
  * Pollster Staged Data is a holding object for an entities poll results
  *
  * @param entity the entity that the data corresponds to
  * @param data the data that corresponds to the entity
  */
case class PollsterEntityData(
  entity: String,
  data: List[EntityData]
)

/**
  * Data point is each data point on the pollster trend line
  *
  * @param time the time/date that the data point corresponds to
  * @param percentage the percentage of vote that the entity had (100.00 Scale)
  */
case class EntityData(
  time: Long,
  percentage: Double
)

/**
  * Data point is each data point on the pollster trend line
  *
  * @param time the time/date index that the data point corresponds to
  * @param percentChange the percentage of vote that the entity had
  */
case class PatternPoint(
  time: Int,
  percentChange: Double
)
