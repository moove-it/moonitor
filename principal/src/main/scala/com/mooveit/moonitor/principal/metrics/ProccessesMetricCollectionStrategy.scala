package com.mooveit.moonitor.principal.metrics

import com.mooveit.moonitor.domain.metrics.{NumberOfProcesses, Metric}

import scala.util.Random

case class NumberOfProcessesStrategy(name: String,
                                     user: String,
                                     status: String)
  extends CollectionStrategy {

  override def collect = Random.nextInt()
}

object ProcessesMetricCollectionStrategy extends CollectionStrategyFactory {

  override def getCollectionStrategy(metric: Metric) = metric match {
    case NumberOfProcesses(name, user, status) =>
      NumberOfProcessesStrategy(name, user, status)
  }
}
