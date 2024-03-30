scalaVersion := "2.13.8" //scala version
val chiselVersion = "3.5.4" //chisel version

lazy val root = (project in file("."))
	.settings(
		name := "crpboy-mips", //project name
		libraryDependencies ++= Seq(
			"edu.berkeley.cs" %% "chisel3" % chiselVersion, // chisel3 dependency
			"edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test" // chisel test dependency
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
