name := "kaizo"
version := "0.7"
scalaVersion := "3.3.6"
maintainer := "hrdavoodi@pm.me"

val http4sVersion = "0.23.32"
val circeVersion = "0.14.15"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.13.0",
  "org.typelevel" %% "cats-effect" % "3.6.3",
  "co.fs2"        %% "fs2-core" % "3.12.2",

  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-core"         % http4sVersion,
  "org.http4s" %% "http4s-client"       % http4sVersion,
  "org.http4s" %% "http4s-server"       % http4sVersion,
  "org.http4s" %% "http4s-circe"        % http4sVersion,

  "org.tpolecat" %% "doobie-core" % "1.0.0-RC10",
  "org.duckdb" % "duckdb_jdbc" % "1.4.1.0",

  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,

  "org.scalamock" %% "scalamock" % "7.5.0" % Test,
  "org.scalamock" %% "scalamock-cats-effect" % "7.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0" % Test,
  "org.scalameta" %% "munit" % "1.2.0" % Test
)

enablePlugins(JavaAppPackaging)

scalacOptions ++= Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xmax-inlines", "256"
)

Compile / console / scalacOptions --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")

Global / cancelable := true
