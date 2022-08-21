package http

import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import http.routes.{GetShortenedRoute, ShortenRoute}
import shortener.Shortener

class Router(shortener: Shortener)
  extends Directives
    with FailFastCirceSupport {
  val routes: Route =
    ShortenRoute(shortener).routes ~
      GetShortenedRoute(shortener).routes ~
      StatsRoute(shortener).routes
}
