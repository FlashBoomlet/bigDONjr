package com.flashboomlet.models

/**
  * Case class for tweet postprocessed data over a time interval
  *
  * @param entityLastName The entity associated with the the tweets
  * @param publishStartDate The start time in millisecond of the interval
  * @param interval the interval of time that the data covers
  * @param totalWords total words in the interval
  * @param totalSentences total sentences in the interval
  * @param totalTitleWordCount average title word count in the interval
  * @param contentCount total number of data in the interval
  * @param topWords 20 or so most common words in the interval, as a map
  * @param uniqueAuthors number of unique authors in the interval
  * @param averageSentiment average sentiment of text in the interval
  * @param strategy The iteration of the strategy used to extract this data
  */
case class PostProcessData(
  entityLastName: String,
  publishStartDate: Long,
  interval: Int,
  minute: Int,
  totalWords: Int,
  totalSentences: Int,
  totalTitleWordCount: Int,
  contentCount: Int,
  topWords: Map[String, Int],
  uniqueAuthors: Int,
  averageSentiment: Double,
  percentPositiveSentiment: Double,
  percentNegativeSentiment: Double,
  strategy: Int
)
