package storage

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import redis.RedisClient
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class Redis(host: String = "127.0.0.1", port: Int = 6379)(implicit val actorSystem: ActorSystem)
  extends Storage with LazyLogging {

  private val redis = RedisClient(host = host, port = port)

  override def save(key: String, obj: String)(implicit encoder: Encoder[String]): Future[Boolean] =
    redis.set(key, obj.asJson.noSpaces)

  override def get(key: String)(implicit decoder: Decoder[String]): Future[Option[String]] =
    redis.get(key).map(_.flatMap(v => decode[String](v.utf8String).toOption))

}
