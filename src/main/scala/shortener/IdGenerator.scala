package shortener

import akka.actor.typed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

object IdGenerator {
  sealed trait Command

  final case class GetValue(replyTo: ActorRef[Option[Long]]) extends Command

  final case class TakeBlocks(possibleBlocks: Option[ServerBlocks]) extends Command

  final case class BlocksSaved(ok: Boolean) extends Command

  def create(serverId: String, blockManagerRef: ActorRef[BlockManager.Command])
            (implicit system: typed.ActorSystem[Nothing]): Behavior[Command] = {
    Behaviors.setup(context => new IdGenerator(context, serverId, blockManagerRef))
  }
}


class IdGenerator(context: ActorContext[IdGenerator.Command],
                  val serverId: String,
                  val blockManagerRef: ActorRef[BlockManager.Command]
                 )(implicit system: typed.ActorSystem[Nothing])
  extends AbstractBehavior[IdGenerator.Command](context) {

  import IdGenerator.*

  implicit val timeout: Timeout = Timeout(FiniteDuration(1, MILLISECONDS))

  private val futureTakeBlocks: Future[TakeBlocks] = blockManagerRef.ask(ref =>
    BlockManager.GetSavedOrCreate(serverId, context.self))
  private var blocks: ServerBlocks = Await.result(futureTakeBlocks, FiniteDuration(1, MILLISECONDS)).possibleBlocks.get
  private var block: Int = blocks.block1.blockIndex
  private var sequence: Short = blocks.block1.possibleSequenceIndex.getOrElse(Short.MinValue)

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
