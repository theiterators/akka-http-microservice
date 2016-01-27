package expenses

case class Date(year: Int, month: Int) {
  def toInt = year * 100 + month

  def >(d: Date) = toInt > d.toInt

  def >=(d: Date) = toInt >= d.toInt

  def <=(d: Date) = toInt <= d.toInt

  def <(d: Date) = toInt < d.toInt

  def ==(d: Date) = toInt == d.toInt

  def !=(d: Date) = toInt == d.toInt

}

case object Date {
  def apply(fromInt: Int): Date = {
    val month = fromInt % 100
    Date((fromInt - month) / 100, month)
  }

  /** from date yyyy-MM
    *
    * @param fromString Date in format yyyy-MM
    * @return
    */
  def apply(fromString:String):Date = {
    val date = """(\d\d\d\d)-(\d\d?)""".r
    fromString match {
      case date(year,month) => Date(year.toInt,month.toInt)
    }

  }
}
