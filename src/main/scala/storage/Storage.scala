package storage

import io.circe.{Decoder, Encoder}
import scala.concurrent.Future

trait Storage {

  def save[T](key: String, obj: T)(implicit encoder: Encoder[T]): Future[Boolean]

  def get[T](key: String)(implicit decoder: Decoder[T]): Future[Option[T]]

  def incBy[T](key: String, inc: Long)(implicit encoder: Encoder[T]): Future[Long]


}
