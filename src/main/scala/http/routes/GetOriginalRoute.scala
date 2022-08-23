package http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import config.HttpConfig
import shortener.Shortener


case class GetOriginalRoute(shortener: Shortener)
  extends Directives {

  val routes: Route =
    get {
      parameter("short") { short =>
        onSuccess(shortener.getOriginal(short)) {
          case Some(url) => complete(url)
          case None => complete(NotFound)
        }
      }
    }
}
