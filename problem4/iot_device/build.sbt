name := "IoT_Device"
version := "0.1"
scalaVersion := "3.0.0"
maintainer := "hamidr.dev@gmail.com"

val catsVersion = "2.6.1"
val catsEffectVersion = "3.1.1"
val http4sVersion = "0.23.0-RC1"
val fs2Ver = "3.0.6"
val scalaTestVersion = "3.2.9"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,

  "org.http4s"    %% "http4s-core" % http4sVersion,
  "org.http4s"    %% "http4s-circe" % http4sVersion,
  "org.http4s"    %% "http4s-blaze-client" % http4sVersion,

  "co.fs2" %% "fs2-core" % fs2Ver,
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
)

Compile / console / scalacOptions --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")

Global / cancelable := true