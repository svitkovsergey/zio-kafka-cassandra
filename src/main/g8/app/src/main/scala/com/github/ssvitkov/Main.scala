package com.github.ssvitkov

import com.github.ssvitkov.app.Application
import com.typesafe.config.ConfigFactory
import wvlet.log.LogSupport
import zio._

object Main extends App with LogSupport {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    for {
      config <- Task(ConfigFactory.load())
      _ <- Application.start(config)
    } yield ()
  }.foldCause(
    e => {
      error(e.prettyPrint); ExitCode.failure
    },
    _ => ExitCode.success
  )
}
