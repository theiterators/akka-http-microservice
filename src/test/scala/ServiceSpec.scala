import akka.event.NoLogging
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser.parse

class ServiceSpec extends AsyncFlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val ip1Info = IpInfo("8.8.8.8", Option("United States"), Option("Ashburn"), Option(39.03), Option(-77.5))
  val ipApiResponse1 = parse("""{"query":"8.8.8.8","status":"success","continent":"North America","continentCode":"NA","country":"United States","countryCode":"US","region":"VA","regionName":"Virginia","city":"Ashburn","district":"","zip":"20149","lat":39.03,"lon":-77.5,"timezone":"America/New_York","offset":-14400,"currency":"USD","isp":"Google LLC","org":"Google Public DNS","as":"AS15169 Google LLC","asname":"GOOGLE","mobile":false,"proxy":false,"hosting":true}""")
  val ip2Info = IpInfo("1.1.1.1", Option("Australia"), Option("South Brisbane"), Option(-27.4766), Option(153.0166))
  val ipApiResponse2 = io.circe.parser.parse("""{"status":"success","country":"Australia","countryCode":"AU","region":"QLD","regionName":"Queensland","city":"South Brisbane","zip":"4101","lat":-27.4766,"lon":153.0166,"timezone":"Australia/Brisbane","isp":"Cloudflare, Inc","org":"APNIC and Cloudflare DNS Resolver project","as":"AS13335 Cloudflare, Inc.","query":"1.1.1.1"}""")
  val ipPairSummary = IpPairSummary(ip1Info, ip2Info)
  val ipApiErrorResponse = parse("""{"status":"fail","message":"invalid query","query":"asdfg"}""")

  override lazy val ipApiConnectionFlow = Flow[HttpRequest].map { request =>
    if (request.uri.toString().endsWith(ip1Info.query))
      HttpResponse(status = OK, entity = marshal(ipApiResponse1))
    else if(request.uri.toString().endsWith(ip2Info.query))
      HttpResponse(status = OK, entity = marshal(ipApiResponse2))
    else
      HttpResponse(status = OK, entity = marshal(ipApiErrorResponse))
  }

  "Service" should "respond to single IP query" in {
    Get(s"/ip/${ip1Info.query}") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpInfo] shouldBe ip1Info
    }

    Get(s"/ip/${ip2Info.query}") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpInfo] shouldBe ip2Info
    }
  }

  it should "respond to IP pair query" in {
    Post(s"/ip", IpPairSummaryRequest(ip1Info.query, ip2Info.query)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[IpPairSummary] shouldBe ipPairSummary
    }
  }

  it should "respond with bad request on incorrect IP format" in {
    Get("/ip/asdfg") ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }

    Post(s"/ip", IpPairSummaryRequest(ip1Info.query, "asdfg")) ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }

    Post(s"/ip", IpPairSummaryRequest("asdfg", ip1Info.query)) ~> routes ~> check {
      status shouldBe BadRequest
      responseAs[String].length should be > 0
    }
  }
}
