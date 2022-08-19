package id_generator

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import storage.Storage
import scala.concurrent.Future
import io.circe._, io.circe.generic.semiauto._
import concurrent.ExecutionContext.Implicits.global
import akka.event.{Logging, LoggingAdapter}
import com.typesafe.scalalogging.LazyLogging

case class BlockStatus(blockIndex: Int, possibleSequenceIndex: Option[Short])

case class ServerBlocks(block1: BlockStatus, block2: BlockStatus)

object BlockManager {
  sealed trait Command

  final case class GetSavedOrCreate(serverId: String, replyTo: ActorRef[IdGenerator.TakeBlocks]) extends Command

  final case class Save(serverId: String, blocks: ServerBlocks, replyTo: ActorRef[IdGenerator.BlocksSaved]) extends Command

  final case class Renovate(serverId: String, blocks: ServerBlocks, replyTo: ActorRef[IdGenerator.TakeBlocks]) extends Command

  def create(storage: Storage): Behavior[Command] = {
    Behaviors.setup(context => new BlockManager(context, storage))
  }
}


class BlockManager(context: ActorContext[BlockManager.Command], val storage: Storage)
  extends AbstractBehavior[BlockManager.Command](context) with LazyLogging {

  import BlockManager.*

  val blocksKey = "blocks"
  val reservedBlocksKey = "reserved"


  def getSavedOrCreate(serverId: String): Future[Option[ServerBlocks]] = {
    val blocksId = s"$blocksKey:$serverId"
    val saved: Future[Option[ServerBlocks]] = storage.get(blocksId)(deriveDecoder[ServerBlocks])
    saved.foreach(possibleBlocks => logger.info(s"Storage return: $possibleBlocks"))
    saved.flatMap {
      case Some(blocks) if blocks.block1.possibleSequenceIndex.isDefined =>
        logger.info(s"getSavedOrCreate: Storage returned valid blocks: $blocks")
        Future(Some(blocks))
      case Some(blocks) =>
        logger.info(s"getSavedOrCreate: Storage returned invalid blocks: $blocks")
        renovate(serverId)
      case _ =>
        logger.info(s"getSavedOrCreate: Storage returned no blocks")
        renovate(serverId)
    }
  }

  def save(serverId: String, blocks: ServerBlocks): Future[Boolean] = {
    val blocksId = s"$blocksKey:$serverId"
    storage.save(blocksId, blocks)(deriveEncoder[ServerBlocks])
  }

  // TODO Management of expired blocks
  def renovate(serverId: String, possibleOldBlocks: Option[ServerBlocks] = None):
  Future[Option[ServerBlocks]] = {

    def reserveNewBlocks(): Future[Option[ServerBlocks]] = {
      logger.info(s"reserveNewBlocks: beginning")
      val futureIndex = storage.incBy(reservedBlocksKey, 2)
      // TODO Manage 24 bits limit
      futureIndex.map(index => {
        val blocks = ServerBlocks(
          BlockStatus(index.toInt - 1, None),
          BlockStatus(index.toInt, None),
        )
        save(serverId, blocks) // TODO check saved ok
        Some(blocks)
      }
      )
    }

    def renovateBlocks(previousBlocks: ServerBlocks): Future[Option[ServerBlocks]] = {
      val futureIndex = storage.incBy(reservedBlocksKey, 1)
      futureIndex.map(index => {
        val blocks = ServerBlocks(previousBlocks.block2, BlockStatus(index.toInt, None))
        save(serverId, blocks) // TODO check saved ok
        Some(blocks)
      })
    }

    logger.info(s"renovate: Beginning, possibleOldBlocks: $possibleOldBlocks ")
    possibleOldBlocks.match {
      case Some(blocks) =>
        logger.info(s"Renovating blocks: $blocks")
        renovateBlocks(blocks)
      case None =>
        logger.info("Reserving new blocks")
        reserveNewBlocks()
    }

  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetSavedOrCreate(serverId, replyTo) =>
        logger.info(s"Begin msg GetSavedOrCreate ServerId: $serverId")
        getSavedOrCreate(serverId).foreach(possibleBlocks => {
          logger.info(s"GetSavedOrCreate ServerId: $serverId answer: $possibleBlocks")
          replyTo ! IdGenerator.TakeBlocks(possibleBlocks)
        })
        this
      case Save(serverId, blocks, replyTo) =>
        logger.info(s"Begin msg Save ServerId: $serverId Blocks: $blocks")
        save(serverId, blocks)
          .foreach(ok => {
            logger.info(s"Save ServerId: $serverId answer: $ok")
            replyTo ! IdGenerator.BlocksSaved(ok)
          }
          )
        this
      case Renovate(serverId, blocks, replyTo) =>
        logger.info(s"Begin msg Renovate ServerId: $serverId Blocks: $blocks")
        renovate(serverId, Some(blocks))
          .foreach(possibleBlocks => {
            logger.info(s"Renovate ServerId: $serverId answer: $blocks")
            replyTo ! IdGenerator.TakeBlocks(possibleBlocks)
          })
        this
    }
  }

}
