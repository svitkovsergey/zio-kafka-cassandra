package com.github.ssvitkov.cassandra.impls

import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.github.ssvitkov.cassandra.CassandraRepository
import zio.Task

class CassandraRepositoryImpl(selectAllstmnt: PreparedStatement) extends CassandraRepository {
  override def selectAll: Task[Unit] = Task{
    println(selectAllstmnt)
  }
}
