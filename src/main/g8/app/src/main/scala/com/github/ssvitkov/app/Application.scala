package com.github.ssvitkov.app

import com.github.ssvitkov.KafkaServers
import com.github.ssvitkov.cassandra.CassandraRepository
import com.github.ssvitkov.config.AppConfig
import com.github.ssvitkov.kafka.KafkaStream
import com.typesafe.config.Config
import wvlet.log.LogSupport
import zio.kafka.consumer.Consumer
import zio.{Task, ZIO}

object Application extends LogSupport {
  def start(config: Config): ZIO[zio.ZEnv, Throwable, Unit] =
    for {
      appConfig <- Task(AppConfig(config.getConfig("app")))
      repoLayer = CassandraRepository.live(config)
      kafkaLayer <- Application.kafkaLayer(config.getConfig("kafka"))
      _ <- job(appConfig).provideCustomLayer(kafkaLayer ++ repoLayer)
    } yield ()

  def kafkaLayer(config: Config) =
    for {
      brokers <- KafkaServers.fromConfig(config)
      consumerSettings <- KafkaServers.consumerSettings(
        brokers.bootstrapServers,
        config.getObject("consumer.kafka-client")
      )
      //uncomment if producer is required
//      producerSettings <- KafkaServers.producerSettings(
//        brokers.bootstrapServers,
//        config.getObject("producer.kafka-client")
//      )
      consumerLayer = Consumer
        .make(
          consumerSettings
        ).toLayer
      //producerLayer = Producer.make(producerSettings).toLayer
    } yield consumerLayer

  def job(appConfig: AppConfig) =
    for {
      stream <- KafkaStream.make(appConfig)
      _ <- stream.start()
    } yield ()
}
