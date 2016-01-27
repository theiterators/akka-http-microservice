package expenses.repo

import expenses.{Date, Expense}

import scala.concurrent.Future

trait ExpensesRepo {
  def get(date: Date): Future[List[Expense]]

  def get(start: Date, end: Date, purpose:Option[String], note:Option[String]): Future[List[Expense]]

  def get(id: Long): Future[Option[Expense]]

  def purposes(search: Option[String]):Future[List[String]]

  def update(expense: Expense): Future[Expense]

  def add(expense: Expense): Future[Expense]

  def delete(expense: Expense): Future[Int] = expense.id.map(delete).getOrElse(Future.successful(0))

  def delete(id: Long): Future[Int]

}
