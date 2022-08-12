package storage

import io.circe.{Decoder, Encoder}
import scala.concurrent.Future

trait Storage {

  def save(key: String, obj: String)(implicit encoder: Encoder[String]): Future[Boolean]

  def get(key: String)(implicit decoder: Decoder[String]): Future[Option[String]]

}
