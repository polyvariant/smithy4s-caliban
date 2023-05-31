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

import weaver._
import CalibanTestUtils._
import smithy4s.example.CityCoordinates
import io.circe.syntax._
import io.circe.Json
import smithy4s.example.MovieTheater
import smithy4s.example.Foo
import smithy4s.Schema
import smithy.api.Length
import smithy4s.Bijection
import smithy4s.example.Ingredient
import smithy4s.example.EnumResult
import smithy4s.example.Rec
import Smithy4sTestUtils._
import smithy4s.Timestamp
import smithy.api.TimestampFormat

object CalibanSchemaTests extends SimpleIOSuite {
  // Workaround for https://github.com/disneystreaming/smithy4s/issues/537
  // works by eagerly instantiating the Timestamp type and everything in its schema
  Schema.timestamp.shapeId.show: Unit

  test("structure schema - all fields required") {
    testQueryResultWithSchema(
      CityCoordinates(latitude = 42.0f, longitude = 21.37f),
      """query {
        |  latitude
        |  longitude
        |}""".stripMargin,
    ).map(
      assert.eql(
        _,
        Json.obj(
          "latitude" := 42.0f,
          "longitude" := 21.37f,
        ),
      )
    )
  }

  test("structure schema - missing optional field") {
    testQueryResultWithSchema(
      MovieTheater(name = None),
      """query {
        |  name
        |}""".stripMargin,
    )
      .map(assert.eql(_, Json.obj("name" := Json.Null)))
  }

  test("structure schema - present optional field") {
    testQueryResultWithSchema(
      MovieTheater(name = Some("cinema")),
      """query {
        |  name
        |}""".stripMargin,
    )
      .map(assert.eql(_, Json.obj("name" := "cinema")))
  }

  test("union schema") {
    testQueryResultWithSchema(
      Foo.StrCase("myString"): Foo,
      """query {
        |  ... on FoostrCase {
        |    str
        |  }
        |}""".stripMargin,
    )
      .map(assert.eql(_, Json.obj("str" := "myString")))
  }

  test("list schema") {

    testQueryResultWithSchema(
      List("a", "b", "c"),
      "query { items }",
    )(Schema.list(Schema.string).nested("items"))
      .map(assert.eql(_, Json.obj("items" := List("a", "b", "c"))))
  }

  test("indexedSeq schema") {
    testQueryResultWithSchema(
      IndexedSeq("a", "b", "c"),
      "query { items }",
    )(Schema.indexedSeq(Schema.string).nested("items"))
      .map(assert.eql(_, Json.obj("items" := List("a", "b", "c"))))
  }

  test("vector schema") {
    testQueryResultWithSchema(
      Vector("a", "b", "c"),
      "query { items }",
    )(Schema.vector(Schema.string).nested("items"))
      .map(assert.eql(_, Json.obj("items" := List("a", "b", "c"))))
  }

  test("set schema") {
    testQueryResultWithSchema(
      Set("a", "b", "c"),
      "query { items }",
    )(Schema.set(Schema.string).nested("items"))
      .map(assert.eql(_, Json.obj("items" := List("a", "b", "c"))))
  }

  test("refinement schema") {

    testQueryResultWithSchema(
      "test",
      "query { item }",
    )(
      Schema
        .string
        .refined(Length(min = Some(1)))
        .nested("item")
    ).map(assert.eql(_, Json.obj("item" := "test")))
  }

  test("bijection schema") {
    testQueryResultWithSchema(
      "test",
      "query { item }",
    )(
      Schema
        .string
        .biject(Bijection[String, String](identity, _.toUpperCase()))
        .nested("item")
    )
      .map(assert.eql(_, Json.obj("item" := "TEST")))
  }

  test("enum schema") {
    testQueryResultWithSchema(
      Ingredient.TOMATO.widen,
      """query { item }""".stripMargin,
    )(Ingredient.schema.nested("item"))
      .map(assert.eql(_, Json.obj("item" := "Tomato")))
  }

  test("int enum schema") {
    testQueryResultWithSchema(
      EnumResult.SECOND.widen,
      """query { item }""".stripMargin,
    )(EnumResult.schema.nested("item"))
      .map(assert.eql(_, Json.obj("item" := 2)))
  }

  test("timestamp schema (default: epoch second)") {
    testQueryResultWithSchema(
      Timestamp.epoch,
      """query { item }""".stripMargin,
    )(Schema.timestamp.nested("item"))
      .map(assert.eql(_, Json.obj("item" := 0L)))
  }

  test("timestamp schema (DATE_TIME)") {
    testQueryResultWithSchema(
      Timestamp.epoch,
      """query { item }""".stripMargin,
    )(Schema.timestamp.addHints(TimestampFormat.DATE_TIME.widen).nested("item"))
      .map(assert.eql(_, Json.obj("item" := "1970-01-01T00:00:00Z")))
  }

  test("timestamp schema (HTTP_DATE)") {
    testQueryResultWithSchema(
      Timestamp.epoch,
      """query { item }""".stripMargin,
    )(Schema.timestamp.addHints(TimestampFormat.HTTP_DATE.widen).nested("item"))
      .map(assert.eql(_, Json.obj("item" := "Thu, 01 Jan 1970 00:00:00 GMT")))
  }

  test("map schema") {
    testQueryResultWithSchema(
      Map("a" -> "b", "c" -> "d"),
      """query {
        | items {
        |   key
        |   value
        |  }
        |}""".stripMargin,
    )(Schema.map(Schema.string, Schema.string).nested("items"))
      .map(
        assert.eql(
          _,
          Json.obj(
            "items" := Json.arr(
              Json.obj(
                "key" := "a",
                "value" := "b",
              ),
              Json.obj(
                "key" := "c",
                "value" := "d",
              ),
            )
          ),
        )
      )
  }

  test("recursive struct") {
    testQueryResultWithSchema(
      Rec(
        name = "level1",
        next = Some(
          Rec(
            name = "level2",
            next = Some(
              Rec(
                name = "level3",
                next = None,
              )
            ),
          )
        ),
      ),
      """query {
        |  item {
        |    name
        |    next {
        |      name
        |      next {
        |        name
        |        next { name }
        |      }
        |    }
        |  }
        |}""".stripMargin,
    )(Rec.schema.nested("item"))
      .map(
        assert.eql(
          _,
          Json.obj(
            "item" := Json.obj(
              "name" := "level1",
              "next" := Json.obj(
                "name" := "level2",
                "next" := Json.obj(
                  "name" := "level3",
                  "next" := Json.Null,
                ),
              ),
            )
          ),
        )
      )
  }
}
