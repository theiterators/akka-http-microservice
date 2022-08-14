import akka.actor.ActorRef
import shortener.IdGenerator

class Shortener(val idGenerator: ActorRef[IdGenerator.Command]) {

  def getShort(url: String): String = {

  }

}
