name := "newmotion"
version := "0.1"

javaOptions += "-XX:MaxPermSize=1024"

lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.6.4"
lazy val json4sVersion     = "3.5.3"


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.newmotion",
      scalaVersion    := "2.12.11"
    )),

    name := "untitled2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,

      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      "org.mockito" % "mockito-core" % "3.3.3" % Test,

      // Json4s libs
      "org.json4s"                  %% "json4s-core"          % json4sVersion,
      "org.json4s"                  %% "json4s-jackson"       % json4sVersion,
      "org.json4s"                  %% "json4s-native"        % json4sVersion,

      "de.heikoseeberger" %% "akka-http-json4s" % "1.20.1",

      "com.github.nscala-time" %% "nscala-time" % "2.18.0"
    )
  )