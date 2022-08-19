package shortener

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.util.Timeout
import io.circe.*
import org.scalatest.*
import org.scalatest.flatspec.*
import encoder.HashidsEncoder

import java.time.LocalDateTime
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS, SECONDS}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.sys.process.Process
import akka.actor.typed.{ActorRef, ActorSystem as TypedActorSystem}
import id_generator.{BlockManager, IdGenerator}
import storage.Redis

class MyShortenerTest extends AnyFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "MyShortener"

  val testKit: ActorTestKit = ActorTestKit()
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val typedActorSystem: TypedActorSystem[Nothing] = testKit.system
  implicit val timeout: Timeout = Timeout(FiniteDuration(1, SECONDS))
  val redis: Redis = storage.Redis()
  val blockManager: ActorRef[BlockManager.Command] = testKit.spawn(BlockManager.create(redis))
  val generator: ActorRef[IdGenerator.Command] = testKit.spawn(IdGenerator.create(serverId = "1", blockManager))
  val encoder: HashidsEncoder = HashidsEncoder()
  private val shortener = MyShortener(generator, encoder, redis)

  override def afterAll(): Unit = {
    actorSystem.terminate()
    typedActorSystem.terminate()
  }

  it should "create a short url and retrieve the original" in {
    Given("an url")
    val url = "https://www.moia.io/de-DE"

    When("create the short")
    val short = Await.result(shortener.getShort(url),
      FiniteDuration(1, SECONDS)).get

    And("retrieve the original")
    val retrievedUrl = Await.result(shortener.getOriginal(short),
      FiniteDuration(10, MILLISECONDS)).get

    Then("The value is the original")
    assert(retrievedUrl == url)
  }

}

