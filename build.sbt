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
  .in(file("modules/core"))
  .settings(
    name := "smithy4s-caliban",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %%% "caliban-cats" % "2.2.1",
      "com.disneystreaming.smithy4s" %%% "smithy4s-core" % smithy4s.codegen.BuildInfo.version,
      "com.disneystreaming" %%% "weaver-cats" % "0.8.3" % Test,
      "io.circe" %% "circe-core" % "0.14.5" % Test,
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Smithy4sCodegenPlugin.defaultSettings(Test),
    scalacOptions --= {
      if (scalaVersion.value.startsWith("3"))
        Seq("-Ykind-projector:underscores")
      else
        Seq()
    },
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("3"))
        Seq("-Ykind-projector")
      else
        Seq()
    },
  )
  .jvmPlatform(Seq(Scala213, Scala3))

lazy val docs = projectMatrix
  .in(file("modules/docs"))
  .settings(
    mdocIn := new File("README.md"),
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % "0.23.19",
      "com.github.ghostdogpr" %%% "caliban-http4s" % "2.2.1",
      "com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % "1.5.0",
    ),
    ThisBuild / githubWorkflowBuild +=
      WorkflowStep.Sbt(
        List("docs/mdoc")
      ),
  )
  .dependsOn(core)
  .jvmPlatform(Seq(Scala213))
  .enablePlugins(MdocPlugin, Smithy4sCodegenPlugin)

lazy val root = project
  .in(file("."))
  .aggregate(core.componentProjects.map(p => p: ProjectReference): _*)
  .enablePlugins(NoPublishPlugin)
