package id_generator

import akka.actor.typed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS, SECONDS}

object IdGenerator {
  sealed trait Command

  final case class GetValue(replyTo: ActorRef[Option[Long]]) extends Command

  final case class TakeBlocks(possibleBlocks: Option[ServerBlocks]) extends Command

  final case class BlocksSaved(was_ok: Boolean) extends Command

  def create(serverId: String, blockManagerRef: ActorRef[BlockManager.Command])(
    implicit system: typed.ActorSystem[Nothing]
  ): Behavior[Command] = {
    Behaviors.setup(context => new IdGenerator(context, serverId, blockManagerRef))
  }
}


class IdGenerator(context: ActorContext[IdGenerator.Command],
                  val serverId: String,
                  val blockManagerRef: ActorRef[BlockManager.Command]
                 )(implicit system: typed.ActorSystem[Nothing])
  extends AbstractBehavior[IdGenerator.Command](context) with LazyLogging {

  import IdGenerator.*

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, SECONDS))

  private val futureTakeBlocks: Future[TakeBlocks] = blockManagerRef.ask(ref =>
    BlockManager.GetSavedOrCreate(serverId, ref)
  )
  private var blocks: ServerBlocks = Await.result(futureTakeBlocks, FiniteDuration(2, SECONDS))
    .possibleBlocks.get
  private var block: Long = blocks.block1.blockIndex
  private var sequence: Short = blocks.block1.possibleSequenceIndex.getOrElse(0)

  def generate(): Option[Long] = {
    if (sequence == Short.MaxValue) {
      block = blocks.block2.blockIndex
      sequence = 0
      blockManagerRef ! BlockManager.Renovate(serverId, blocks, context.self)
    }
    sequence = (sequence + 1).toShort
    logger.info(s"generate block: ${block.toHexString} sequence: ${sequence.toLong.toHexString}")
    val generated: Option[Long] = Some(block << 16 | sequence)
    logger.info(s"generate return ${generated.map(_.toHexString)}")
    generated
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetValue(replyTo) =>
        replyTo ! generate()
        this
      case TakeBlocks(possibleBlocks) =>
        if (possibleBlocks.isEmpty) {
          logger.error("IDGenerator didn't get more blocks, dying...")
        }
        blocks = possibleBlocks.get
        this
      case BlocksSaved(ok) =>
        context.log.info(s"Log saved $ok")
        this
    }
  }

}
