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

import caliban.GraphQL
import caliban.RootResolver
import caliban.interop.cats.FromEffect
import caliban.interop.cats.implicits._
import caliban.schema._
import cats.effect.std.Dispatcher
import smithy4s.kinds.FunctorInterpreter
import smithy4s.kinds.FunctorAlgebra
import smithy4s.Service
import smithy.api.Readonly
import caliban.introspection.adt.__Field
import smithy4s.Endpoint
import smithy4s.schema.CompilationCache
import caliban.InputValue
import cats.effect.kernel.Async
import cats.effect.kernel.Resource

object CalibanGraphQLInterpreter {

  def server[Alg[_[_, _, _, _, _]], F[_]: Async](
    impl: FunctorAlgebra[Alg, F]
  )(
    implicit
    service: Service[Alg]
  ): Resource[F, GraphQL[Any]] = Dispatcher.parallel[F].map { implicit dispatcher =>
    val abv = new ArgBuilderVisitor(CompilationCache.make[ArgBuilder])
    val csv = new CalibanSchemaVisitor(CompilationCache.make[Schema[Any, *]])

    val (queries, mutations) = service.endpoints.partition(_.hints.has[Readonly])

    val interp = service.toPolyFunction(impl)

    val querySchema: Schema[Any, service.FunctorInterpreter[F]] =
      Schema.obj(name = "Queries", description = None)(implicit fa =>
        queries.map(endpointToSchema[F].apply(_, abv, csv)).toList
      )

    val mutationSchema: Schema[Any, service.FunctorInterpreter[F]] =
      Schema.obj(name = "Mutations", description = None)(implicit fa =>
        mutations.map(endpointToSchema[F].apply(_, abv, csv)).toList
      )

    caliban.graphQL(
      RootResolver(
        queryResolver = Option.when(queries.nonEmpty)(interp),
        mutationResolver = Option.when(mutations.nonEmpty)(interp),
        subscriptionResolver = None,
      )
    )(
      SubscriptionSchema.unitSubscriptionSchema,
      querySchema,
      mutationSchema,
      Schema.unitSchema,
    )
  }

  private def endpointToSchema[F[_]: Dispatcher] = new EndpointToSchemaPartiallyApplied[F]

  // "partially-applied type" pattern used here to give the compiler a hint about what F is
  // but let it infer the remaining type parameters
  final class EndpointToSchemaPartiallyApplied[F[_]: Dispatcher] private[smithy4scaliban] {

    def apply[Op[_, _, _, _, _], I, E, O, SI, SO](
      e: Endpoint[Op, I, E, O, SI, SO],
      abv: ArgBuilderVisitor,
      csv: CalibanSchemaVisitor,
    )(
      implicit fa: FieldAttributes
    ): (__Field, FunctorInterpreter[Op, F] => Step[Any]) = {
      val hasArgs = e.input.shapeId != smithy4s.schema.Schema.unit.shapeId

      if (hasArgs)
        // function type
        Schema.fieldWithArgs(e.name.uncapitalize) { (interp: FunctorInterpreter[Op, F]) => (i: I) =>
          interp(e.wrap(i))
        }(
          Schema.functionSchema[Any, Any, I, F[O]](
            e.input.compile(abv),
            e.input.compile(csv),
            catsEffectSchema(
              FromEffect.forDispatcher,
              e.output.compile(csv),
            ),
          ),
          fa,
        )
      else {
        val inputDecodedFromEmptyObj: I =
          e
            .input
            .compile(abv)
            .build(InputValue.ObjectValue(Map.empty))
            .toTry
            .get

        Schema.field(e.name.uncapitalize) { (interp: FunctorInterpreter[Op, F]) =>
          interp(e.wrap(inputDecodedFromEmptyObj))
        }(
          catsEffectSchema(
            FromEffect.forDispatcher,
            e.output.compile(csv),
          ),
          fa,
        )
      }
    }

  }

  private implicit final class StringOps(val s: String) extends AnyVal {

    def uncapitalize: String =
      s match {
        case "" => ""
        case _  => s"${s.head.toLower}${s.tail}"
      }

  }

}
