package shortener

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class IdGeneratorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private implicit val ec: ExecutionContext = ExecutionContext.global

  "IdGenerator" must {
    "answer an id" in {
      val generator = testKit.spawn(IdGenerator.create(serverId = 1))
      val futurePossibleId: Future[Option[Long]] = generator.ask(ref => IdGenerator.GetValue(ref))
      futurePossibleId.map(v => assert(v.isDefined))
    }
  }
}
