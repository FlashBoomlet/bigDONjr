package com.flashboomlet.models

/**
  * Object stored in database to track the most recently post processed chunk
  * @param startTime The start time in milliseconds of the most recently post processed chunk
  * @param strategy The iteration of the post processing strategy
  */
case class RecentTweetPostProcess(
  startTime: Long,
  strategy: Int
)
