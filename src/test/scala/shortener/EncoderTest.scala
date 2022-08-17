package shortener

import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AsyncFlatSpec

class EncoderTest extends AsyncFlatSpec with GivenWhenThen {

  behavior of "Encoder"

  "Encoder" should "retrieve string with length<=8" in {
    Given("Some longs")
    val numbers = List(0x0l, 0xFFFFFFFFl, 0xFFFFFFFFFl)

    When("encoded")
    val encodes = numbers.map(Encoder.encode)

    Then("the size <=8")
    assert(encodes.forall(_.length <= 8))

  }

}
