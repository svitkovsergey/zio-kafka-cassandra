package com.github.ssvitkov.config

import com.typesafe.config.Config

import java.util.UUID

case class AppConfig(config: Config, appUUID: UUID = UUID.randomUUID()) {
  val topics = ???
}
