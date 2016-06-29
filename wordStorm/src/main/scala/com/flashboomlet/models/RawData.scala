package com.flashboomlet.models

import com.flashboomlet.data.models.Sentiment

/**
  * Case Class for raw data to find temporal data in
  */
case class RawData(
  entityLastName: String,
  sentiment: Sentiment,
  wordCount: Int,
  sentenceCount: Int,
  titleWordCount: Int,
  wordOccurrences: Map[String, Int],
  author: String
)
