package org.polyvariant.smithy4scaliban

import caliban.interop.cats.FromEffect
import caliban.interop.cats.implicits._
import caliban.schema._
import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import smithy4s.Service

object CalibanGraphQLInterpreter {

  def server[Alg[_[_, _, _, _, _]], F[_]: Async: Dispatcher](
    implicit
    service: Service[Alg]
  ): Schema[Any, service.Impl[F]] =
    // todo: renaming to account for graphql conventions (camelCase ops etc.)?
    // todo: splitting into queries/mutations?
    Schema.obj(name = service.id.name, description = None)(implicit fa =>
      service.endpoints.map(endpointToSchema[F].apply(service)(_))
    )

  private def endpointToSchema[F[_]: Async: Dispatcher] = new EndpointToSchemaPartiallyApplied[F]

  // "partially-applied type" pattern used here to give the compiler a hint about what F is
  // but let it infer the remaining type parameters
  final class EndpointToSchemaPartiallyApplied[F[_]: Async: Dispatcher] private[smithy4scaliban] {

    def apply[Alg[_[_, _, _, _, _]], I, E, O, SI, SO](
      service: Service[Alg]
    )(
      e: service.Endpoint[I, E, O, SI, SO]
    )(
      implicit fa: FieldAttributes
    ) =
      Schema.fieldWithArgs[service.Impl[F], I](e.name) { alg =>
        val interp = service.toPolyFunction(alg)

        i => interp(e.wrap(i))
      }(
        Schema.functionSchema[Any, Any, I, F[O]](
          e.input.compile(ArgBuilderVisitor),
          e.input.compile(CalibanSchemaVisitor),
          catsEffectSchema(
            FromEffect.forDispatcher,
            e.output.compile(CalibanSchemaVisitor),
          ),
        ),
        fa,
      )

  }

}
