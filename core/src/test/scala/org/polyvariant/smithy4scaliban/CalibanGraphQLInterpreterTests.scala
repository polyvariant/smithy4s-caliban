/*
 * Copyright 2023 Polyvariant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.polyvariant.smithy4scaliban

import cats.effect.IO
import cats.effect.std.Dispatcher
import io.circe.Json
import io.circe.syntax._
import smithy4s.example.ListCitiesOutput
import smithy4s.example.WeatherService

import CalibanTestUtils._
import smithy4s.example.City
import smithy4s.example.RefreshCitiesOutput

object CalibanGraphQLInterpreterTests extends weaver.SimpleIOSuite {

  private val weatherImpl: WeatherService[IO] =
    new WeatherService.Default[IO](IO.stub) {

      override def listCities(
      ): IO[ListCitiesOutput] = IO(
        ListCitiesOutput(
          List(
            City("London"),
            City("NYC"),
          )
        )
      )

      override def refreshCities(): IO[RefreshCitiesOutput] = IO(RefreshCitiesOutput("ok"))

    }

  test("WeatherService service query interpreter: query") {
    Dispatcher
      .parallel[IO]
      .use { implicit d =>
        testApiResult(
          CalibanGraphQLInterpreter.server(weatherImpl),
          """query {
            |  listCities {
            |    cities {
            |      name
            |    }
            |  }
            |}""".stripMargin,
        )
      }
      .map(
        assert.eql(
          _,
          Json.obj(
            "listCities" := Json.obj(
              "cities" := Json.arr(
                Json.obj(
                  "name" := "London"
                ),
                Json.obj(
                  "name" := "NYC"
                ),
              )
            )
          ),
        )
      )
  }
  test("WeatherService service query interpreter: mutation") {
    Dispatcher
      .parallel[IO]
      .use { implicit d =>
        testApiResult(
          CalibanGraphQLInterpreter.server(weatherImpl),
          """mutation {
            |  refreshCities {
            |    result
            |  }
            |}""".stripMargin,
        )
      }
      .map(
        assert.eql(
          _,
          Json.obj(
            "refreshCities" := Json.obj(
              "result" := "ok"
            )
          ),
        )
      )
  }

}
