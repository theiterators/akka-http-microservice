package shortener

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

object IdGenerator {
  sealed trait Command

  final case class GetValue(replyTo: ActorRef[Option[Long]]) extends Command

  final case class TakeBlocks(possibleBlocks: Option[ServerBlocks]) extends Command

  def create(serverId: String): Behavior[Command] = {
    Behaviors.setup(context => new IdGenerator(context, serverId))
  }
}


class IdGenerator(context: ActorContext[IdGenerator.Command],
                  val serverId: String,
                  val blockManagerRef: ActorRef[BlockManager]
                 )
  extends AbstractBehavior[IdGenerator.Command](context) {

  import IdGenerator.*

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, MILLISECONDS))

  private var blocks: ServerBlocks = Future.wait(blockManagerRef.ask(ref =>
    BlockManager.GetSavedOrCreate(serverId, context.self))).get
  private var block: Int = blocks.block1.blockIndex
  private var sequence: Short = blocks.block1.sequenceIndex.getOrElse(Short.MinValue)

  def generate(): Option[Long] = {
    if (sequence == Short.MaxValue) {
      block = blocks.block2.blockIndex
      sequence = Short.MinValue
      blockManagerRef ! BlockManager.Renovate(serverId, blocks, context.self)
    }
    sequence = (sequence + 1).toShort
    return Some(block << 16 | sequence)
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetValue(replyTo) =>
        replyTo ! generate()
        this
      case TakeBlocks(possibleBlocks) =>
        // TODO log no block available
        blocks = possibleBlocks.get
        this
    }
  }

}
