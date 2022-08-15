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

  override def save[T](key: String, obj: T)(implicit encoder: Encoder[T]): Future[Boolean] =
    redis.set(key, obj.asJson.noSpaces)

  override def get[T](key: String)(implicit decoder: Decoder[T]): Future[Option[T]] =
    redis.get(key).map(_.flatMap(v => decode[T](v.utf8String).toOption))

  def incBy[T](key: String, inc: Long)(implicit encoder: Encoder[T]): Future[Long] =
    redis.incrby(key, inc)


}
