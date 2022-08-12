package storage

import akka.actor.ActorSystem
import org.scalatest.*
import flatspec.*
import org.scalatest.matchers.should.Matchers.*
import scala.concurrent.ExecutionContext

class RedisTest extends AnyFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "Redis"

  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private val redis = Redis()

  override def afterAll(): Unit = actorSystem.terminate()

  "Redis" should "retrieve the value associate with the key" in {
    Given("key and value")
    val key = "test_key"
    val value = "test_value"

    When("Saved and load from redis")
    val futureValue = redis.save(key, value).flatMap(_ => redis.get(key))

    Then("The value is the original")
    futureValue.map(v => assert(v.contains(value)))
  }

}
