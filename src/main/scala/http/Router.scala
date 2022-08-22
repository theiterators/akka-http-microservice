package http

import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import http.routes.{GetOriginalRoute, PostUrlRoute}
import shortener.Shortener

class Router(shortener: Shortener)
  extends Directives
    with FailFastCirceSupport {
  val routes: Route =
    PostUrlRoute(shortener).routes ~
      GetOriginalRoute(shortener).routes
}