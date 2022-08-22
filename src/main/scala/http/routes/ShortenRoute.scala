package http.routes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import config.HttpConfig
import http.HttpUtils.validateUri
import shortener.Shortener

case class ShortenRoute(shortener: Shortener)
  extends Directives
    with FailFastCirceSupport {

  val routes: Route =
    pathEndOrSingleSlash {
      post {
        extractRequest { request =>
          onSuccess(shortener.getShort(request.uri.toString())) {
            result => {
              result match {
                case Some(short) =>
                  complete(short)
                case _ =>
                  complete(BadRequest)
              }
            }
          }
        }
      }
    }
}
