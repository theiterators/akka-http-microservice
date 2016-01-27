package expenses.repo

import expenses.{Date, Expense}

import scala.concurrent.Future

class MemoryExpensesRepo extends ExpensesRepo {

  var list: List[Expense] = List.empty[Expense]

  override def get(date: Date): Future[List[Expense]] = {
    val r = list.filter(_.date == date)
    Future.successful(r)
  }

  override def add(expense: Expense): Future[Expense] = {
    val id = if (list.isEmpty) {
      0
    } else {
      list.flatMap(_.id).max + 1
    }
    val copy: Expense = expense.copy(id = Some(id))
    list = copy :: list
    Future.successful(copy)
  }

  override def delete(id: Long): Future[Int] = {
    println("[Memory] Removing ")
    val filter: List[Expense] = list.filter(_.id.exists(_ == id))
    val removed = list.size - filter.size
    list = filter
    Future.successful(removed)
  }

  override def get(id: Long): Future[Option[Expense]] = Future.successful(list.find(_.id == id))


  override def update(expense: Expense): Future[Expense] = {
    list = expense :: list.filterNot(_.id == expense.id)
    Future.successful(expense)
  }

  override def get(start: Date, end: Date, purpose: Option[String], note: Option[String]): Future[List[Expense]] = {
    val r: List[Expense] = list
      .filter(e => e.date >= start && e.date<= end)
      .filter(e => purpose.getOrElse("").toLowerCase.contains(e.purpose.toLowerCase))
      .filter(e => note.getOrElse("").toLowerCase.contains(e.note.toLowerCase))
    Future.successful(r)
  }

  override def purposes(search: Option[String]): Future[List[String]] = {
    if (search.isDefined){
      Future.successful(list.map(_.purpose).filter(_.contains(search.get)).toSeq.toList)
    } else {
      Future.successful(list.map(_.purpose).toSeq.toList)
    }
  }
}
