import sbt._
import Keys._

import explicitdeps.ExplicitDepsPlugin.autoImport._
import sbtbuildinfo._
import BuildInfoKeys._

object BuildHelper {
  private val SilencerVersion = "1.4.3"

  val testDeps        = Seq("org.scalacheck"  %% "scalacheck"  % "1.14.2"        % "test")
  val compileOnlyDeps = Seq("com.github.ghik" % "silencer-lib" % SilencerVersion % Provided cross CrossVersion.full)

  private val stdOptions = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked"
  )

  private val std2xOptions = Seq(
    "-Xfatal-warnings",
    "-language:higherKinds",
    "-language:existentials",
    "-explaintypes",
    "-Yrangepos",
    "-Xsource:2.13",
    "-Xlint:_,-type-parameter-shadow",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )

  val buildInfoSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
    buildInfoPackage := "zio",
    buildInfoObject := "BuildInfo"
  )

  def extraOptions(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) =>
        std2xOptions
      case Some((2, 12)) =>
        Seq(
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused:_,imports",
          "-Ywarn-unused:imports",
          "-opt:l:inline",
          "-opt-inline-from:zio.internal.**",
          "-Ypartial-unification",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Xfuture"
        ) ++ std2xOptions
      case Some((2, 11)) =>
        Seq(
          "-Ypartial-unification",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Xexperimental",
          "-Ywarn-unused-import",
          "-Xfuture"
        ) ++ std2xOptions
      case _ => Seq.empty
    }

  def stdSettings(prjName: String) = Seq(
    name := s"$prjName",
    scalacOptions := stdOptions,
    crossScalaVersions := Seq("2.13.0", "2.12.8", "2.11.12"),
    scalaVersion in ThisBuild := crossScalaVersions.value.head,
    scalacOptions := stdOptions ++ extraOptions(scalaVersion.value),
    libraryDependencies ++= compileOnlyDeps ++ testDeps ++ Seq(
      compilerPlugin("org.typelevel"   %% "kind-projector" % "0.10.3"),
      compilerPlugin("com.github.ghik" % "silencer-plugin" % SilencerVersion cross CrossVersion.full)
    ),
    parallelExecution in Test := true,
    incOptions ~= (_.withLogRecompileOnMacro(false))
  )
}
