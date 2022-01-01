package com.github.ssvitkov.cassandra

import com.github.ssvitkov.cassandra.impls.CassandraRepositoryImpl
import com.typesafe.config.Config
import zio.cassandra.CassandraSession
import zio.{Task, ZIO, blocking}
import zio.cassandra.service.CassandraSession

trait CassandraRepository {}

object CassandraRepository {
  def init(config: Config, session: CassandraSession): ZIO[Any, Throwable, Unit] = for {
    _ <- session.execute(createKeyspace).when(config.getBoolean("cassandra.create-keyspace"))
    _ <- session.execute(createTable)
  } yield ()

  def prepare(session: CassandraSession) =
      for {
        selectAll <- session.prepare(selectAll)
        //..
      } yield (selectAll)


  def make(config: Config) = for {
    session <- CassandraSession.make(config.getConfig("cassandra"))
    service <- (for {
      _ <- init(config, session)
      _ <- prepare(session)
    } yield new CassandraRepositoryImpl()).toManaged_
  } yield service

}