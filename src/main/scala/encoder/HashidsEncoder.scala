package encoder

import org.pico.hashids.Hashids

class HashidsEncoder(salt: String = "My salt is MOIA") extends Encoder {

  val hasher = Hashids.reference(salt,
    0, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_"
  )

  override def encode(n: Long): String = hasher.encode(n)

}
