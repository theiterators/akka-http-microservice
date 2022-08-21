package http

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object HttpUtils {
  def validateUri(urlField: String): Future[Option[String]] = {
    try {
      val _ = new URL(urlField)
      Future(Some(urlField))
    } catch {
      case _: Throwable =>
        Future(None)
    }
  }
}