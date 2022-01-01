import sbt._

object Dependencies {
  val zioVersion = "1.0.12"
  val zioKafkaVersion = "0.17.1"

  val zioDependencies = Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-streams" % zioVersion,
    "dev.zio" %% "zio-kafka" % zioKafkaVersion
  )

  val cassandraDependencies = Seq(
    "io.github.jsfwa" %% "zio-cassandra" % "1.0.6"
  )

  val commonDependencies = Seq(
    "org.apache.kafka" %% "kafka" % "2.7.0",
    "org.wvlet.airframe" %% "airframe-log" % "20.5.1",
    "org.slf4j" % "slf4j-jdk14" % "1.7.21",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.11.0" % Compile,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.11.0" % Provided,
    "com.typesafe" % "config" % "1.4.1",
    "com.github.ghostdogpr" %% "caliban" % "1.2.1"
  )

  val testDependencies = Seq(
    "dev.zio" %% "zio-test" % zioVersion,
    "dev.zio" %% "zio-test-sbt" % zioVersion,
    "com.dimafeng" %% "testcontainers-scala-core" % "0.39.9",
    "com.dimafeng" %% "testcontainers-scala-cassandra" % "0.39.9",
    "com.dimafeng" %% "testcontainers-scala-kafka" % "0.39.9",

  ).map(_ % Test)

  val default = zioDependencies ++ commonDependencies ++ cassandraDependencies ++ testDependencies
}
