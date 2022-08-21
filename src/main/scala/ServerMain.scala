import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import http.Service
import akka.actor.typed.ActorSystem as TypedActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*

import scala.io.StdIn

object ServerMain extends App {
  implicit val system: TypedActorSystem[Nothing] =
    TypedActorSystem(Behaviors.empty, "url-shortener")
  implicit val executor: ExecutionContextExecutor = system.executionContext

  val config = ConfigFactory.load()
  //  val logger = Logging(system, "url_shortener")

  //  Http().newServerAt(config.getString("http.interface"),
  //    config.getInt("http.port")).bindFlow(routes)

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done


}
