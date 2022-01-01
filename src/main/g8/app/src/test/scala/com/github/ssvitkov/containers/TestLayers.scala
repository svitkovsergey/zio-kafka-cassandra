package com.github.ssvitkov.containers

import com.datastax.oss.driver.api.core.CqlSession
import com.dimafeng.testcontainers.{CassandraContainer, KafkaContainer}
import com.github.ssvitkov.KafkaServers
import com.typesafe.config.ConfigFactory
import wvlet.log.LogSupport
import zio.cassandra.CassandraSession
import com.github.ssvitkov.cassandra.CassandraRepository
import zio.cassandra.service.CassandraSession
import zio.clock.Clock
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer
import zio.test.{TestFailure, environment}
import zio.{Has, ZIO, ZLayer}

import java.net.InetSocketAddress

object TestLayers extends LogSupport {
  val layerCassandra = ZTestCassandra.cassandra

  val layerSession: ZLayer[Has[CassandraContainer], TestFailure[Nothing], Has[CassandraSession]] = (for {
    cassandra <- ZTestCassandra[CassandraContainer].toManaged_
    session <- {
      val address = new InetSocketAddress(cassandra.containerIpAddress, cassandra.mappedPort(9042))
      val builder = CqlSession
        .builder()
        .addContactPoint(address)
        .withLocalDatacenter("datacenter1")
      CassandraSession.make(builder)
    }
  } yield session).toLayer.mapError(TestFailure.die)

  val committerLayer = (for {
    cassandra <- ZTestCassandra[CassandraContainer].toManaged_
    committer <- CassandraRepository.make(
      ConfigFactory.parseString(s"cassandra.port = \${cassandra.mappedPort(9042)}")
        .withFallback(ConfigFactory.load("cassandra.conf"))
    )
  } yield committer).toLayer


  val producerLayer = (for {
    kafka <- ZIO.service[KafkaContainer].toManaged_
    producerSettings <- KafkaServers
      .producerSettings(List(kafka.bootstrapServers),
        ConfigFactory.load("application.conf").getConfig("kafka").getObject("producer.kafka-client")).toManaged_
    pl <- Producer.make(producerSettings)
  } yield pl).toLayer

  val consumerLayer = (for {
    kafka <- ZIO.service[KafkaContainer].toManaged_
    consumerSettings <- KafkaServers
      .consumerSettings(List(kafka.bootstrapServers),
        ConfigFactory.load("application.conf").getConfig("kafka").getObject("consumer.kafka-client")).toManaged_
    cl <- Consumer.make(consumerSettings)
  } yield cl).toLayer

  val managed = ZTestKafka.managed().toLayer ++ environment.liveEnvironment

  val kafkaLayer = managed >>> (producerLayer ++ consumerLayer)

  val layer = kafkaLayer ++ (layerCassandra >>> layerSession) ++ ((environment.liveEnvironment ++ layerCassandra) >>> committerLayer) ++ Clock.live
}
