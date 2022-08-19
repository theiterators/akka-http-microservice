package shortener

import akka.actor.ActorSystem
import akka.actor.typed.ActorSystem as TypedActorSystem
import akka.actor.typed.ActorRef
import com.typesafe.scalalogging.LazyLogging
import id_generator.IdGenerator
import encoder.Encoder
import storage.Storage
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import redis.RedisClient
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS, SECONDS}

class MyShortener(val generator: ActorRef[IdGenerator.Command],
                  val encoder: Encoder,
                  val storage: Storage)(
                   implicit val typedActorSystem: TypedActorSystem[Nothing],
                   actorSystem: ActorSystem,
                   timeout: Timeout
                 ) extends Shortener with LazyLogging {

  val prefix = "Short"

  def getShort(url: String): Future[Option[String]] = {
    logger.info(s"getShort url: $url")
    implicit val timeout: Timeout = Timeout(FiniteDuration(1, SECONDS))
    val futurePossibleId: Future[Option[Long]] = generator.ask(ref => IdGenerator.GetValue(ref))

    val futurePossiblePair: Future[Option[(String, String)]] = futurePossibleId.map(possibleId =>
      possibleId.map(id => (encoder.encode(id), url)))
    futurePossiblePair.foreach {
      case Some(Tuple2(short, url)) => storage.save(key = s"$prefix:$short", obj = url)
      case _ =>
    }
    futurePossiblePair.map {
      case Some(Tuple2(short, _)) =>
        logger.info(s"getShort returning: $short")
        Some(short)
      case x =>
        logger.info(s"getShort returning: $x")
        None
    }
  }

  def getOriginal(short: String): Future[Option[String]] = {
    logger.info(s"getOriginal short: $short")
    val futurePossibleUrl = storage.get(key = s"$prefix:$short")(Decoder[String])
    futurePossibleUrl.foreach(possibleUrl => logger.info(s"getOriginal returning: $possibleUrl"))
    futurePossibleUrl
  }

}
