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
