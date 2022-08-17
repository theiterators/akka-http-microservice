package storage

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest.*
import flatspec.*
import io.circe.*
import scala.concurrent.duration.{FiniteDuration, SECONDS, MILLISECONDS}
import scala.sys.process.Process
import scala.concurrent.{Await, ExecutionContext}
import java.time.LocalDateTime

class RedisTest extends AsyncFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "Redis"

  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private val redis = Redis()

  override def afterAll(): Unit = actorSystem.terminate()

  it should "retrieve the value associate with the key" in {
    Given("key and value")
    val key = "redis_test_key"
    val value = "redis_test_value"

    When("Saved and load from redis")
    val futureValue = redis.save(key, value).flatMap(_ => redis.get(key)(Decoder[String]))

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