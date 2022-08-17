package shortener

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.{ActorSystem, typed}
import akka.util.Timeout
import io.circe.Decoder
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import org.scalatest.flatspec.AsyncFlatSpec
import storage.Redis

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import akka.actor.typed.scaladsl.AskPattern.*

import java.time.LocalDateTime

class BlockManagerTest extends AsyncFlatSpec with GivenWhenThen with BeforeAndAfterAll {

  behavior of "BlockManager"

  val testKit: ActorTestKit = ActorTestKit()
  private implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val timeout: Timeout = Timeout(FiniteDuration(200, MILLISECONDS))
  implicit val system: typed.ActorSystem[Nothing] = testKit.system
  private implicit val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  val redis: Redis = storage.Redis()
  val blockManager: ActorRef[BlockManager.Command] = testKit.spawn(BlockManager.create(redis))

  it should "reserve new blocks for a server the first time" in {
    Given("A new server id")
    val serverId = s"TestServerId_${LocalDateTime.now()}"

    When("Ask for the server blocks")
    val futureTakeBlocks: Future[IdGenerator.TakeBlocks] =
      blockManager.ask(ref => BlockManager.GetSavedOrCreate(serverId, ref))

    Then("it must create the blocks")
    futureTakeBlocks.map(takeBlocks =>
      assert(takeBlocks.possibleBlocks.isDefined)
    )

    And("the block ids must be consecutive")
    futureTakeBlocks.map(takeBlocks => {
      val id1 = takeBlocks.possibleBlocks.get.block1.blockIndex
      val id2 = takeBlocks.possibleBlocks.get.block2.blockIndex
      assert(id2 == id1 + 1)
    })

    And("the sequences must be None")
    futureTakeBlocks.map(takeBlocks => {
      val possibleSequence1 = takeBlocks.possibleBlocks.get.block1.possibleSequenceIndex
      val possibleSequence2 = takeBlocks.possibleBlocks.get.block2.possibleSequenceIndex
      assert(possibleSequence1.isEmpty)
      assert(possibleSequence2.isEmpty)
    })
  }

  it should "save the blocks and return the saved next time" in {
    Given("A new server id")
    val serverId = s"TestServerId_${LocalDateTime.now()}"

    And("A sequence")
    val sequence: Short = 5

    When("Ask for the server blocks")
    val futureTakeBlocks: Future[IdGenerator.TakeBlocks] =
      blockManager.ask(ref => BlockManager.GetSavedOrCreate(serverId, ref))

    And("Update the sequence")
    val futureUpdatedBlocks = futureTakeBlocks.map(takeBlocks => {
      val blocks = takeBlocks.possibleBlocks.get
      blocks.copy(block1 = blocks.block1.copy(possibleSequenceIndex = Some(sequence)))
    })

    And("Save the blocks")
    val futureBlocksSaved: Future[IdGenerator.BlocksSaved] =
      futureUpdatedBlocks.flatMap(updatedBlocks =>
        blockManager.ask(ref => BlockManager.Save(serverId, updatedBlocks, ref))
      )

    And("Ask again the server blocks")
    val futureNewBlocks: Future[IdGenerator.TakeBlocks] =
      futureBlocksSaved.flatMap(_ =>
        blockManager.ask(ref => BlockManager.GetSavedOrCreate(serverId, ref))
      )

    Then("the new blocks must be the expected")
    futureNewBlocks.map(takeBlocks => {
      val blocks = takeBlocks.possibleBlocks.get
      assert(blocks.block1.possibleSequenceIndex.get == sequence)
      assert(blocks.block2.possibleSequenceIndex.isEmpty)
    })

  }

  it should "return new block is are not saved" in {
    Given("a new server id")
    val serverId = s"TestServerId_${LocalDateTime.now()}"

    When("ask for the server blocks")
    val fistTakeBlocks: Future[IdGenerator.TakeBlocks] =
      blockManager.ask(ref => BlockManager.GetSavedOrCreate(serverId, ref))

    And("ask again without saving")
    val futureNewBlocks: Future[IdGenerator.TakeBlocks] =
      fistTakeBlocks.flatMap(_ =>
        blockManager.ask(ref => BlockManager.GetSavedOrCreate(serverId, ref))
      )

    Then("the new blocks must new")
    fistTakeBlocks.zip(futureNewBlocks).map { case (firstBlocks, newBlocks) => {
      val firstBlockIndex2 = firstBlocks.possibleBlocks.get.block1.blockIndex
      val newBlockIndex1 = newBlocks.possibleBlocks.get.block1.blockIndex
      val newBlockIndex2 = newBlocks.possibleBlocks.get.block2.blockIndex
      assert(newBlockIndex1 > firstBlockIndex2)
      assert(newBlockIndex2 == newBlockIndex1 + 1)
    }
    }

  }


}
