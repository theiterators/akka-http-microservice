package id_generator

import akka.actor.{ActorSystem, typed}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, *}
import flatspec.*
import org.scalatest.matchers.should.Matchers.*
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import scala.concurrent.ExecutionContext
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import storage.Redis

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}


class IdGeneratorTest extends AnyFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "IdGenerator"

  val testKit: ActorTestKit = ActorTestKit()
  private implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val timeout: Timeout = Timeout(FiniteDuration(1, MILLISECONDS))
  implicit val system: typed.ActorSystem[Nothing] = testKit.system
  private implicit val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  val redis: Redis = storage.Redis()
  val blockManager: ActorRef[BlockManager.Command] = testKit.spawn(BlockManager.create(redis))
  val generator: ActorRef[IdGenerator.Command] = testKit.spawn(IdGenerator.create(serverId = "1", blockManager))

  "IdGenerator" should "generate an id" in {
    Given("The generator")


    When("Ask for a new id")
    val futurePossibleId: Future[Option[Long]] = generator.ask(ref => IdGenerator.GetValue(ref))

    Then("it must create one")
    futurePossibleId.map(v => assert(v.isDefined))
  }

  "IdGenerator generated ids" should "be crescent" in {
    Given("The generator")

    When("Ask for 2 ids")
    val futurePossibleId1: Future[Option[Long]] = generator.ask(ref => IdGenerator.GetValue(ref))
    val futurePossibleId2: Future[Option[Long]] = generator.ask(ref => IdGenerator.GetValue(ref))
    val sequence = Future.sequence(List(futurePossibleId1, futurePossibleId2))

    Then("it must be crescent")
    sequence.map(list => list.head.get < list.tail.head.get)
  }

}
