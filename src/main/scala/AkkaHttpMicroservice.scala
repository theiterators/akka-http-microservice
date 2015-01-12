import akka.actor.ActorSystem
import akka.http.Http
import akka.http.client.RequestBuilding
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.model.{HttpResponse, HttpRequest}
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.unmarshalling.Unmarshal
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

object AkkaHttpMicroservice extends App {
  private val config = ConfigFactory.load()
  private val interface = config.getString("http.interface")
  private val port = config.getInt("http.port")

  private implicit val actorSystem = ActorSystem()
  private implicit val materializer = FlowMaterializer()
  private implicit val dispatcher = actorSystem.dispatcher

  Http().bind(interface = interface, port = port).startHandlingWith {
    logRequestResult("akka-http-microservice") {
      get {
        complete("Hello world!")
      }
    }
  }
}
