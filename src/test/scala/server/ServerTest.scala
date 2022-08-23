package server

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.GivenWhenThen
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import akka.http.scaladsl.model.*
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}

class ServerTest extends AsyncFlatSpec with GivenWhenThen {

  behavior of "Server"

  implicit val system: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty, "ServerTest")

  "Server" should "shorten and url and return the original given the short" in {
    Given("the request")
    val url = "https://www.moia.io/de-DE"
    val postRequest = Post(uri = s"http://localhost:9001/?url=$url")

    When("post given the url")
    val futureResponsePost: Future[HttpResponse] = Http().singleRequest(postRequest)

    Then("the response contains a short/|short|<=8 ")
    futureResponsePost.foreach(response => {
      assert(response.status.isSuccess())
      assert(response.entity.toString.length <= 8)
    })

    And("when get given the short")
    val futureShort = futureResponsePost.map(_.entity.toString)
    val futureGetRequest = futureShort.map(short => Get(uri = s"http://localhost:9001/?short=$short"))
    val futureGetResponse = futureGetRequest.flatMap(request => Http().singleRequest(request))

    Then("it returns the original url")
    futureGetResponse.foreach(response => {
      assert(response.status.isSuccess())
      assert(response.entity.toString == url)
    })
    succeed
  }

}
