import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.event.{Logging, LoggingAdapter}
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.client.RequestBuilding
import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

import java.io.IOException
import scala.concurrent.{ExecutionContext, Future}
import scala.math._

enum IpApiResponseStatus {
  case Success, Fail
}
case class IpApiResponse(status: IpApiResponseStatus, message: Option[String], query: String, country: Option[String], city: Option[String], lat: Option[Double], lon: Option[Double])

case class IpInfo(query: String, country: Option[String], city: Option[String], lat: Option[Double], lon: Option[Double])

case class IpPairSummaryRequest(ip1: String, ip2: String)

case class IpPairSummary(distance: Option[Double], ip1Info: IpInfo, ip2Info: IpInfo)

object IpPairSummary {
  def apply(ip1Info: IpInfo, ip2Info: IpInfo): IpPairSummary = IpPairSummary(calculateDistance(ip1Info, ip2Info), ip1Info, ip2Info)

  private def calculateDistance(ip1Info: IpInfo, ip2Info: IpInfo): Option[Double] = {
    (ip1Info.lat, ip1Info.lon, ip2Info.lat, ip2Info.lon) match {
      case (Some(lat1), Some(lon1), Some(lat2), Some(lon2)) =>
        // see http://www.movable-type.co.uk/scripts/latlong.html
        val φ1 = toRadians(lat1)
        val φ2 = toRadians(lat2)
        val Δφ = toRadians(lat2 - lat1)
        val Δλ = toRadians(lon2 - lon1)
        val a = pow(sin(Δφ / 2), 2) + cos(φ1) * cos(φ2) * pow(sin(Δλ / 2), 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        Option(EarthRadius * c)
      case _ => None
    }
  }

  private val EarthRadius = 6371.0
}

trait Protocols extends ErrorAccumulatingCirceSupport {
  import io.circe.generic.semiauto._
  implicit val ipApiResponseStatusDecoder: Decoder[IpApiResponseStatus] = Decoder.decodeString.map(s => IpApiResponseStatus.valueOf(s.capitalize))
  implicit val ipApiResponseDecoder: Decoder[IpApiResponse] = deriveDecoder
  implicit val ipInfoDecoder: Decoder[IpInfo] = deriveDecoder
  implicit val ipInfoEncoder: Encoder[IpInfo] = deriveEncoder
  implicit val ipPairSummaryRequestDecoder: Decoder[IpPairSummaryRequest] = deriveDecoder
  implicit val ipPairSummaryRequestEncoder: Encoder[IpPairSummaryRequest] = deriveEncoder
  implicit val ipPairSummaryEncoder: Encoder[IpPairSummary] = deriveEncoder
  implicit val ipPairSummaryDecoder: Decoder[IpPairSummary] = deriveDecoder
}

trait Service extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContext

  def config: Config
  val logger: LoggingAdapter

  lazy val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("services.ip-api.host"), config.getInt("services.ip-api.port"))
  
  // Please note that using `Source.single(request).via(pool).runWith(Sink.head)` is considered anti-pattern. It's here only for the simplicity.
  // See why and how to improve it here: https://github.com/theiterators/akka-http-microservice/issues/32
  def ipApiRequest(request: HttpRequest): Future[HttpResponse] = Source.single(request).via(ipApiConnectionFlow).runWith(Sink.head)

  def fetchIpInfo(ip: String): Future[String | IpInfo] = {
    ipApiRequest(RequestBuilding.Get(s"/json/$ip")).flatMap { response =>
      response.status match {
        case OK =>
          Unmarshal(response.entity).to[IpApiResponse].map { ipApiResponse =>
          ipApiResponse.status match {
            case IpApiResponseStatus.Success => IpInfo(ipApiResponse.query,ipApiResponse.country, ipApiResponse.city, ipApiResponse.lat, ipApiResponse.lon)
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
    logRequestResult("pekko-http-microservice") {
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

object PekkoHttpMicroservice extends App with Service {
  override implicit val system: ActorSystem = ActorSystem()
  override implicit val executor: ExecutionContext = system.dispatcher

  override val config = ConfigFactory.load()
  override val logger = Logging(system, "pekkoHttpMicroservice")

  Http().newServerAt(config.getString("http.interface"), config.getInt("http.port")).bindFlow(routes)
}
