package http

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import java.io.IOException
import scala.concurrent.{ExecutionContext, Future}
import scala.math._
import akka.http.scaladsl.Http
import com.typesafe.config.Config

enum IpApiResponseStatus {
  case Success, Fail
}


trait Service extends Serialization {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContext

  def config: Config

  val logger: LoggingAdapter

  lazy val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("services.ip-api.host"),
      config.getInt("services.ip-api.port")
    )

  def ipApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request)
    .via(ipApiConnectionFlow).runWith(Sink.head)

  def fetchIpInfo(ip: String): Future[String | IpInfo] = {
    ipApiRequest(RequestBuilding.Get(s"/json/$ip")).flatMap { response =>
      response.status match {
        case OK =>
          Unmarshal(response.entity).to[IpApiResponse].map { ipApiResponse =>
            ipApiResponse.status match {
              case IpApiResponseStatus.Success => IpInfo(ipApiResponse.query, ipApiResponse.country, ipApiResponse.city, ipApiResponse.lat, ipApiResponse.lon)
              case IpApiResponseStatus.Fail => s"""ip-api request failed with message: ${ipApiResponse.message.getOrElse("")}"""
            }
          }
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"ip-api request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

  val routes: Route = {
    logRequestResult("akka-http-microservice") {
      pathPrefix("ip") {
        (get & path(Segment)) { ip =>
          complete {
            fetchIpInfo(ip).map[ToResponseMarshallable] {
              case ipInfo: IpInfo => ipInfo
              case errorMessage: String => BadRequest -> errorMessage
            }
          }
        } ~
          (post & entity(as[IpPairSummaryRequest])) { ipPairSummaryRequest =>
            complete {
              val ip1InfoFuture = fetchIpInfo(ipPairSummaryRequest.ip1)
              val ip2InfoFuture = fetchIpInfo(ipPairSummaryRequest.ip2)
              ip1InfoFuture.zip(ip2InfoFuture).map[ToResponseMarshallable] {
                case (info1: IpInfo, info2: IpInfo) => IpPairSummary(info1, info2)
                case (errorMessage: String, _) => BadRequest -> errorMessage
                case (_, errorMessage: String) => BadRequest -> errorMessage
              }
            }
          }
      }
    }
  }
}
