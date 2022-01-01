package com.github.ssvitkov.kafka

import com.github.ssvitkov.cassandra.CassandraRepository
import com.github.ssvitkov.config.AppConfig
import wvlet.log.LogSupport
import zio.{Schedule, ZIO}
import zio.clock.Clock
import zio.kafka.consumer.Subscription.Topics
import zio.kafka.consumer.{CommittableRecord, Consumer}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import scala.util.Try

@scala.annotation.nowarn //unused cassandra
class KafkaStream(
  cassandraRepo: CassandraRepository,
  topics: Set[String]
                 ) extends LogSupport {

  def committableStream[R](stream: ZStream[R, Throwable, CommittableRecord[Try[Long], String]]): ZStream[R with Clock, Throwable, Unit] =
    stream.bufferSliding(1).mapM{m =>
      info(s"{m.offset.topicPartition}] committing \${m.offset.offset}")
      m.offset.commitOrRetry(Schedule.recurs(10))
    }

  def process(stream: ZStream[Any, Throwable, CommittableRecord[Try[Long], String]]): ZStream[Any, Throwable, CommittableRecord[Try[Long], String]] = ???

  def start() = Consumer.subscribeAnd(Topics(topics))
    .partitionedStream(Serde.long.asTry, Serde.string)
    .mapMParUnordered(Byte.MaxValue){
      case (tp, s) =>
        info(s"Start processing [\$tp]")
        committableStream(process(s)).runDrain
    }.runDrain
}

object KafkaStream {
  def make(appConfig: AppConfig) =
    for {
      cassandraRepository <- ZIO.service[CassandraRepository]
    } yield new KafkaStream(cassandraRepo = cassandraRepository, appConfig.topics)
}
