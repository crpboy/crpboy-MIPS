scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "crpboy-MIPS",
    libraryDependencies ++= Seq(
      // "edu.berkeley.cs" %% "chisel3"    % "3.5.4",
      // "edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test",
      // "org.scalameta" % "semanticdb-scalac" % "4.7.7" cross CrossVersion.full,
      "org.chipsalliance" %% "chisel" % "6.3.0",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Yrangepos",
    ),
    // scalacOptions ++= Seq("-nowarn"),
    addCompilerPlugin(
      // "edu.berkeley.cs" % "chisel3-plugin" % "3.5.4" cross CrossVersion.full,
      "org.chipsalliance" % "chisel-plugin" % "6.3.0" cross CrossVersion.full,
    ),
  )
