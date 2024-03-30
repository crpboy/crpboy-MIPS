
scalaVersion := "2.13.8" // scala version
val chiselVersion = "3.5.4" // chisel version

lazy val root = (project in file(".")) // create a sbt project definition
	.settings(
		name := "crpboy-MIPS", // set project name
		libraryDependencies ++= Seq(
			"edu.berkeley.cs" %% "chisel3" % chiselVersion, // chisel3 dependency
			"edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test", // chisel test dependency
			"org.scalameta" % "semanticdb-scalac" % "4.7.7" cross CrossVersion.full,
		),
		scalacOptions ++= Seq(
			"-language:reflectiveCalls",
			"-deprecation",
			"-feature",
			"-Xcheckinit",
			"-P:chiselplugin:genBundleElements",
			"-Yrangepos",
		),
		addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
	)
