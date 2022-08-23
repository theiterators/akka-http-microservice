package http.routes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import config.HttpConfig
import shortener.Shortener

case class PostUrlRoute(shortener: Shortener)
  extends Directives
    with FailFastCirceSupport {

  val routes: Route =
    post {
      parameter("url") { url =>
        onSuccess(shortener.getShort(url)) {
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
