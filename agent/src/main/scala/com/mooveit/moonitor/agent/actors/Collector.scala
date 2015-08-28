package com.mooveit.moonitor.agent.actors

import akka.actor.{Actor, Cancellable, Props}
import com.mooveit.moonitor.agent.CollectionStrategyLoader
import com.mooveit.moonitor.domain.metrics._
import com.mooveit.moonitor.agent.actors.Agent.MetricCollected
import com.mooveit.moonitor.agent.actors.Collector.{ChangeFrequency, Collect}

import scala.concurrent.Future
import scala.concurrent.duration._

class Collector(conf: MetricConfiguration) extends Actor {

  import context.dispatcher

  private var scheduledCollection: Cancellable = _
  private val collectionStrategy =
    CollectionStrategyLoader.
      loadCollectionStrategy(conf.metricId, conf.packageName)

  override def preStart() = {
    scheduleCollection(conf.frequency)
  }

  override def postStop() = {
    scheduledCollection.cancel()
  }

  def scheduleCollection(frequency: Int) =
    scheduledCollection = context.system.scheduler.
      schedule(0.seconds, frequency.millis, self, Collect)

  def changeFrequency(newFrequency: Int) = {
    scheduledCollection.cancel()
    scheduleCollection(newFrequency)
  }

  def updateAgent(result: MetricResult) =
    context.parent !
      MetricCollected(conf.metricId, result)

  override def receive = {
    case ChangeFrequency(newFrequency) => changeFrequency(newFrequency)

    case Collect => Future { collectionStrategy.collect } map updateAgent
  }
}

object Collector {

  def props(conf: MetricConfiguration) = Props(new Collector(conf))

  case object Collect

  case class ChangeFrequency(newFrequency: Int)
}
