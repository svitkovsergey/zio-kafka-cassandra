package com.github.ssvitkov.containers

import zio.{Has, RIO, Tag, TaskManaged, ZIO, ZLayer, ZManaged}
import zio.test.TestFailure
import com.dimafeng.testcontainers.CassandraContainer
import com.dimafeng.testcontainers.GenericContainer.DockerImage
import org.testcontainers.lifecycle.Startable
import org.testcontainers.utility.DockerImageName


object ZTestCassandra {

  def cassandra: ZLayer[Any, TestFailure[Nothing], Has[CassandraContainer]] =
    managed(CassandraContainer(DockerImageName.parse("cassandra:3.11.6"))).toLayer.mapError(TestFailure.die)

  def managed[T <: Startable](container: T): TaskManaged[T] =
    ZManaged.makeEffect{
      container.start()
      container
    }(_.stop())

  def apply[C: Tag]: RIO[Has[C], C] =
    ZIO.service[C]
}
