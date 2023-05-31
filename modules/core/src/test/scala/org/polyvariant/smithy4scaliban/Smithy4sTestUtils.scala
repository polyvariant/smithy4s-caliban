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

import smithy4s.schema.Schema
import smithy4s.Bijection

object Smithy4sTestUtils {

  implicit class SchemaOps[A](schema: Schema[A]) {
    def nested(label: String): Schema[A] =
      Schema.struct(schema.required[A](label, identity(_)))(identity(_))

    def biject[B](bijection: Bijection[A, B]): Schema[B] = Schema.bijection(schema, bijection)
  }

}
