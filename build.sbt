scalaVersion := "2.13.8"
val useChisel6 = true

lazy val root = (project in file("."))
  .settings(
    name := "crpboy-MIPS",
    libraryDependencies ++= Seq(
      // "edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test",
      // "org.scalameta" % "semanticdb-scalac" % "4.7.7" cross CrossVersion.full,
      if (useChisel6) {
        "org.chipsalliance" %% "chisel" % "6.3.0"
      } else {
        // "org.chipsalliance" %% "chisel" % "3.6.0"
        "edu.berkeley.cs" %% "chisel3" % "3.5.4"
      },
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Yrangepos",
    ),
    addCompilerPlugin(
      if(useChisel6) {
        "org.chipsalliance" % "chisel-plugin" % "6.3.0" cross CrossVersion.full
      } else {
        // "org.chipsalliance" % "chisel-plugin" % "3.6.0" cross CrossVersion.full
        "edu.berkeley.cs" % "chisel3-plugin" % "3.5.4" cross CrossVersion.full
      },
    ),
  )
