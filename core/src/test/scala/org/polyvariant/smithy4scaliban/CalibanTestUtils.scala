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

import caliban.RootResolver
import caliban.interop.cats.implicits._
import caliban.schema.Schema
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._

object CalibanTestUtils {
  private implicit val rt: zio.Runtime[Any] = zio.Runtime.default

  def testQueryResult[A](
    api: A,
    q: String,
  )(
    implicit
    aSchema: Schema[Any, A]
  ): IO[Json] = caliban
    .graphQL(RootResolver(api))
    .interpreterAsync[IO]
    .flatMap(_.executeAsync[IO](q))
    .map(_.data.asJson)

  def testQueryResultWithSchema[A: smithy4s.Schema](
    api: A,
    q: String,
  ): IO[Json] =
    testQueryResult(api, q)(
      implicitly[smithy4s.Schema[A]].compile(CalibanSchemaVisitor)
    )

}
