package expenses

import scala.math.BigDecimal

case class Expense(id: Option[Long], date: Date, purpose: String, amount: BigDecimal, note: String)


