ThisBuild / tlBaseVersion := "0.1"
ThisBuild / organization := "org.polyvariant"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))
ThisBuild / tlSonatypeUseLegacyHost := false

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val compilerPlugins = List(
  crossPlugin("org.polyvariant" % "better-tostring" % "0.3.17")
)

val Scala213 = "2.13.10"
val Scala3 = "3.3.0"

ThisBuild / scalaVersion := Scala213

ThisBuild / tlFatalWarnings := false
ThisBuild / tlFatalWarningsInCi := false

val commonSettings = Seq(
  libraryDependencies ++= compilerPlugins
)

lazy val core = projectMatrix
  .settings(
    name := "smithy4s-caliban",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %%% "smithy4s-core" % smithy4s.codegen.BuildInfo.version,
      "com.disneystreaming" %%% "weaver-cats" % "0.8.3" % Test,
    ),
  )
  .jvmPlatform(Seq(Scala213, Scala3))
  .jsPlatform(Seq(Scala213, Scala3))
  .nativePlatform(Seq(Scala3))

lazy val root = tlCrossRootProject
  .aggregate(core)
