package com.flashboomlet.actors

import akka.actor.Actor
import com.flashboomlet.proccessing.PostProcessor

/**
  * Created by trill on 6/29/16.
  */
class ArticlePostProcessTickActor extends Actor {


  /** What to do on receiving a tick message */
  def receive: Actor.Receive = {
    case AkkaConstants.Tick => PostProcessor.postProcessNYTArticles()
  }
}
