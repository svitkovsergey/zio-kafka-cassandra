inThisBuild(
  List(
    scalaVersion := "2.13.6",
    organization := "com.github.ssvitkov",
    scalafixDependencies += "com.nequissimus" %% "sort-imports" % "0.5.5",
    resolvers := Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      Resolver.sonatypeRepo("public")
    ),
    scalacOptions ++= Seq(
      "-encoding",
      "utf-8",
      "unchecked",
      "-explaintypes",
      "-Yrangepos",
      "-Ywan-unused",
      "-Ymacro-annotations",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Xfatal-warnings",
      "-Werror",
      "-Wconf:any:error"
    ),
    Test / parallelExecution := false,
    Test / fork := false
  )
)

val commonSettings = Seq(
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
)

lazy val app =
  (project in file("app"))
    .dependsOn(utils)
    .settings(commonSettings:_*)
    .settings(
      name := "myapp-app",
      libraryDependencies ++= Dependencies.default
    )

lazy val utils =
  (project in file("utils"))
    .settings(commonSettings:_*)
    .settings(
      name := "myapp-utils",
      libraryDependencies ++= Dependencies.default
    )

lazy val root = (project in file("."))
  .aggregate(app, utils)
  .settings(
    name := "myapp-root"
  )
