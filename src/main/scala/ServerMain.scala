import akka.actor.ActorSystem
import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import server.Service

object ServerMain extends App with Service {
  override implicit val system: ActorSystem = ActorSystem()
  override implicit val executor: ExecutionContext = system.dispatcher

  override val config = ConfigFactory.load()
  override val logger = Logging(system, "url_shortener")

  Http().newServerAt(config.getString("http.interface"),
    config.getInt("http.port")).bindFlow(routes)
}
