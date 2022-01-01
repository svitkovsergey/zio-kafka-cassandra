package com.github.ssvitkov.containers

import com.dimafeng.testcontainers.KafkaContainer
import zio.{Has, Task, ZLayer, ZManaged}

object ZTestKafka {
  def managed(): ZManaged[Any, Throwable, KafkaContainer] =
    Task {
      val container = KafkaContainer()
      container.start()
      container
    }.toManaged(c => Task(c.stop()).orDie)

  def live(): ZLayer[Any, Throwable, Has[KafkaContainer]] = managed().toLayer
}
