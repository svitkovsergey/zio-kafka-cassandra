package com.github.ssvitkov

import com.typesafe.config.{Config, ConfigObject, ConfigValue}
import zio.Task
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings

import scala.collection.mutable

case class KafkaServers(bootstrapServers: List[String])

object KafkaServers {
  def consumerSettings(brokers: List[String], obj: ConfigValue): Task[ConsumerSettings] = Task {
    ConsumerSettings(brokers).withProperties(obj.toStringMap)
  }

  def producerSettings(brokers: List[String], obj: ConfigValue): Task[ProducerSettings] = Task {
    ProducerSettings(brokers).withProperties(obj.toStringMap)
  }

  def fromConfig(config: Config): Task[KafkaServers] = Task {
    KafkaServers(
      bootstrapServers = config
        .getString("bootstrap-servers")
        .split("(,|\\\s+|;)")
        .map(_.trim)
        .filter(_.nonEmpty)
        .toList
    )
  }

  implicit class toStringMap(val v: ConfigValue) extends AnyVal {
    def toStringMap: Map[String, String] = {
      val result = mutable.Map.empty[String, String]

      def traverse(path: String, r: ConfigValue): Unit =
        r match {
          case o: ConfigObject =>
            o.keySet().forEach { key =>
              val nextPath = if (path.isEmpty) key else path + "." + key
              traverse(nextPath, o.get(key))
            }
          case x => result.addOne(path -> x.unwrapped().toString)
        }

      traverse("", v)
      result.toMap
    }
  }
}
