package shortener

import org.pico.hashids._

object Encoder {

  val hasher = Hashids.reference("My salt is MOIA",
    0, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_"
  )

  def encode(n: Long): String = hasher.encode(n)

}
