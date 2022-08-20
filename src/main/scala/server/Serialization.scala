package server

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.{Decoder, Encoder}

trait Serialization extends ErrorAccumulatingCirceSupport {

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
