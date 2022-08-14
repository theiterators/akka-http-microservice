package shortener

import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec

class EncoderTest extends AnyFlatSpec with GivenWhenThen {

  behavior of "Encoder"

  "Encoder" should "retrieve string with lenght<=8" in {
    Given("Some longs")
    val numbers = List(0x0l, 0xFFFFFFFFl, 0xFFFFFFFFFl)

    When("encoded")
    val encodeds = numbers.map(Encoder.encode)

    println(encodeds)

    Then("the size <=8")
    assert(encodeds.forall(_.length <= 8))

  }

}
