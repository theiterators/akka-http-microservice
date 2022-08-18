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

class RedisTest extends AsyncFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "MyShortener"

  val testKit: ActorTestKit = ActorTestKit()
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val typedActorSystem: TypedActorSystem[Nothing] = testKit.system
  val redis: Redis = storage.Redis()
  val blockManager: ActorRef[BlockManager.Command] = testKit.spawn(BlockManager.create(redis))
  val generator: ActorRef[IdGenerator.Command] = testKit.spawn(IdGenerator.create(serverId = "1", blockManager))
  val encoder = HashidsEncoder()
  private val shortener = MyShortener(generator, encoder, redis)

  override def afterAll(): Unit = {
    actorSystem.terminate()
    typedActorSystem.terminate()
  }

  it should "create a short url and retrieve the original" in {
    Given("an url")
    val url = "https://www.moia.io/de-DE"

    When("create the short")
    val futureShort: Future[Option[String]] = shortener.getShort(url)

    And("retrieve the original")
    val futureUrl: Future[Option[String]] = futureShort.flatMap(
      (possibleShort: Option[String]) =>
        possibleShort.flatMap((short: String) => shortener.getOriginal(short))
    )

    Then("The value is the original")
    futureValue.map(v => assert(v.contains(value)))
  }

}

class RedisRestartTest extends AnyFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "Redis"

  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private val redis = Redis()

  override def afterAll(): Unit = actorSystem.terminate()

  "Redis server" should "keep the value when restart" in {
    Given("key and value")
    val key = "redis_test_restart_key"
    val value = s"redis_test_restart_value: ${LocalDateTime.now()}"

    When("Save")
    Await.result(redis.save(key, value)(Encoder[String]), FiniteDuration(1, SECONDS))

    And("Restart the server")
    val pb = Process("bin/restart_redis.sh")
    val exitCode = pb.!
    assert(exitCode == 0)

    And("Get the value")
    val result = Await.result(redis.get(key)(Decoder[String]), FiniteDuration(2, SECONDS))

    Then("The value is the original")
    assert(result.contains(value))
  }


}