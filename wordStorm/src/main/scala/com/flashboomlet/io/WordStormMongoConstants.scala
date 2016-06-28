package com.flashboomlet.io

/**
  * Contains MongoDB database constants for the wordstorm project
  */
trait WordStormMongoConstants {

  /** String constant for the article post process data collection in Mongodb */
  val ArticlePostProcessDatasCollectionString = "articlepostprocessdatas"

  /** String constant for the article post process data collection in Mongodb */
  val TweetPostProcessDatasCollectionString = "tweetpostprocessdatas"


  object PostProcessDataConstants {

    val EntityLastNameString = "entity_last_name"

    val PublishStartDateString = "publish_start_date"

    val IntervalString = "interval"

    val TotalWordsString = "total_words"

    val TotalSentencesString = "total_sentences"

    val TotalTitleWordCountString = "average_title_word_count"

    val ContentCountString = "content_count"

    val TopWordsString = "top_words"

    val UniqueAuthorsString = "unique_authors"

    val AverageSentimentString = "average_sentiment"
  }
}
