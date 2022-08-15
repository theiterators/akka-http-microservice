package shortener

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import storage.Storage
import scala.concurrent.Future
import io.circe._, io.circe.generic.semiauto._

case class BlockStatus(blockIndex: Int, sequenceIndex: Option[Short])

case class ServerBlocks(block1: BlockStatus, block2: BlockStatus)

object BlockManager {
  sealed trait Command

  final case class GetSavedOrCreate(serverId: String, replyTo: ActorRef[IdGenerator.TakeBlocks]) extends Command

  final case class Save(serverId: String, blocks: ServerBlocks, replyTo: ActorRef[Boolean]) extends Command

  final case class Renovate(serverId: String, blocks: ServerBlocks, replyTo: ActorRef[IdGenerator.TakeBlocks]) extends Command

  def create(serverId: String, storage: Storage): Behavior[Command] = {
    Behaviors.setup(context => new BlockManager(context, storage))
  }
}


class BlockManager(context: ActorContext[BlockManager.Command], val storage: Storage)
  extends AbstractBehavior[BlockManager.Command](context) {

  import BlockManager.*

  val reservedBlocksKey = "reserved"

  def getSavedOrCreate(serverId: String): Future[Option[ServerBlocks]] = {
    val blocksId = s"blocks:$serverId"
    val saved: Future[Option[ServerBlocks]] = storage.get(blocksId)(deriveDecoder[ServerBlocks])
    saved.map {
      case possibleBlocks@Some(blocks) => possibleBlocks
      case None => renovate(serverId).flatten
    }
  }

  def save(serverId: String, blocks: ServerBlocks): Future[Boolean] = {
    val blocksId = s"blocks:$serverId"
    storage.save(blocksId, blocks)
  }

  // TODO Management of expired blocks
  def renovate(serverId: String, possibleOldBlocks: Option[ServerBlocks] = None):
  Future[Option[ServerBlocks]] = {

    def reserveNewBlocks(): Future[Option[ServerBlocks]] = {
      val futureIndex = storage.incBy(reservedBlocksKey, 2)
      futureIndex.map(index => {
        val blocks = ServerBlocks(
          BlockStatus(index - 1, None),
          BlockStatus(index, None),
        )
        save(serverId, blocks) // TODO check saved ok
        Some(blocks)
      }
      )
    }

    def renovateBlocks(previosBlocks: ServerBlocks): Future[Option[ServerBlocks]] = {
      val futureIndex = storage.incBy(reservedBlocksKey, 1)
      futureIndex.map(index => {
        val blocks = ServerBlocks(previosBlocks.block2, BlockStatus(index, None))
        save(serverId, blocks) // TODO check saved ok
        Some(blocks)
      })
    }

    possibleOldBlocks.match {
      case Some(blocks) => renovateBlocks(blocks)
      case None => reserveNewBlocks()
    }

  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetSavedOrCreate(serverId, replyTo) =>
        replyTo ! getSavedOrCreate(serverId)
        this
      case Save(serverId, blocks, replyTo) =>
        replyTo ! save(serverId, blocks)
        this
      case Renovate(serverId, blocks, replyTo) =>
        replyTo ! renovate(serverId, blocks)
        this
    }
  }

}
