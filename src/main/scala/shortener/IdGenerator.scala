package shortener

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior

object IdGenerator {
  sealed trait Command

  final case class GetValue(replyTo: ActorRef[Option[Long]]) extends Command

  def create(serverId: Byte): Behavior[Command] = {
    Behaviors.setup(context => new IdGenerator(context, serverId))
  }
}

/*
Generate 7 Bytes number id
First: Server identifier
Next 4: Lower bytes of milliseconds from 1970.01.01
Next 2: Sequence in the millisecond

Maximum 256 servers
The identifier can repeat every 78 years
Each server can generate 0xFFFD maximum per millisecond

*/

class IdGenerator(context: ActorContext[IdGenerator.Command], serverId: Byte)
  extends AbstractBehavior[IdGenerator.Command](context) {

  import IdGenerator.*

  private val _serverId: Long = serverId
  private var lastMillisecond = 0
  private var sequence: Short = Short.MinValue

  def generate(): Option[Long] = {
    val currentMillisecond = System.currentTimeMillis().toInt
    if (currentMillisecond > lastMillisecond) {
      lastMillisecond = currentMillisecond
      sequence = Short.MinValue
    } else if (currentMillisecond < lastMillisecond ||
      (currentMillisecond == lastMillisecond && sequence == Short.MaxValue))
      return None

    sequence = (sequence + 1).toShort
    return Some(_serverId << 48 | currentMillisecond << 16 | sequence)
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetValue(replyTo) =>
        replyTo ! generate()
        this
    }
  }

}
