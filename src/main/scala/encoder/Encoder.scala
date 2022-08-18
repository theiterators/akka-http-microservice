package encoder

import org.pico.hashids.Hashids

trait Encoder {

  def encode(n: Long): String

}
