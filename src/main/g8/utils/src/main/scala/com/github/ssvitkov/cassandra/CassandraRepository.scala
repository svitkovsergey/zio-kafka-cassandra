package com.github.ssvitkov.cassandra

import com.github.ssvitkov.cassandra.impls.CassandraRepositoryImpl
import com.typesafe.config.Config
import zio.{Task, ZIO, ZEnv, ZManaged}
import zio.cassandra.CassandraSession
import zio.cassandra.service.CassandraSession

trait CassandraRepository {
  def selectAll: Task[Unit]
}

object CassandraRepository extends Statements {
  def make(config: Config): ZManaged[ZEnv, Throwable, CassandraRepository] = for {
    session <- CassandraSession.make(config.getConfig("cassandra"))
    service <- (for {
      _ <- init(config, session)
      prepared <- prepare(session)
    } yield new CassandraRepositoryImpl(prepared)).toManaged_
  } yield service

  def init(config: Config, session: CassandraSession): ZIO[Any, Throwable, Unit] = for {
    _ <- session.execute(createKeyspace).when(config.getBoolean("cassandra.create-keyspace"))
    _ <- session.execute(createTable)
  } yield ()

  def prepare(session: CassandraSession) =
    for {
      selectAll <- session.prepare(selectAll)
      //..
    } yield (selectAll)


  def live(config: Config) = make(config).toLayer
}