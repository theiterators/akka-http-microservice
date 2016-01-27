package expenses.repo

import expenses.{Date, Expense}
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import slick.jdbc.JdbcBackend.Database



class SlickExpensesRepo(val driver: JdbcProfile,val db:Database) extends ExpensesRepo {
  import driver.api._
  implicit val ec = ExecutionContext.global

  // Import the Scala API from the driver
  val expenses: TableQuery[DbExpenses] = TableQuery[DbExpenses]
  val tupleToExpense: ((Long, String, String, Int, BigDecimal)) => Expense = {
    case (id, purpose, note, date1, amount) =>
      Expense(Some(id), Date(date1), purpose, amount, note)
  }

  def expenseToTuple(e: Expense) = (e.id.getOrElse(0l), e.purpose, e.note, e.date.toInt, e.amount)

//  val setupAction: DBIO[Unit] = DBIO.seq(
//    expenses.schema.create,
//    expenses +=(1, "car", "99 Market Street", 201404, BigDecimal(10)),
//    expenses +=(2, "car", "99 Market Street", 201404, BigDecimal(50)),
//    expenses +=(3, "car", "note", 201404, BigDecimal(50)),
//    expenses +=(4, "house", "note", 201403, BigDecimal(50)),
//    expenses +=(5, "house", "99 Market Street", 201405, BigDecimal(30)),
//    expenses +=(6, "car", "99 Market Street", 201406, BigDecimal(30)),
//    expenses +=(7, "travel", "n", 201406, BigDecimal(30))
//  )
//
//  db.run(setupAction)

  override def get(date: Date): Future[List[Expense]] = {
    val filter: Query[DbExpenses, (Long, String, String, Int, BigDecimal), Seq] = expenses.filter(_.date === date.toInt)
    val run: Future[Seq[(Long, String, String, Int, BigDecimal)]] = db.run(filter.result)
    val run1: Future[List[Expense]] = run.map { x =>
      x.map(tupleToExpense).toList
    }
    run1
  }

  override def add(expense: Expense): Future[Expense] = {
    val insertQuery = expenses returning expenses.map(_.id) into ((expense, id) => expense.copy(_1 = id))
    val run = db.run(insertQuery.+=(expenseToTuple(expense)))
    run.map(t=>expense.copy(id = Some(t._1)))
//    Future.successful(expense)
  }

  override def delete(id: Long): Future[Int] = {
    val run: Future[Int] = db.run(expenses.filter(_.id === id).delete)
    run
  }

  override def get(id: Long): Future[Option[Expense]] = {
    val run = db.run(expenses.filter(_.id === id).result.headOption)
    run.map(x => x.map(tupleToExpense))
  }

  override def update(expense: Expense): Future[Expense] = {
    db.run(
      expenses
        .filter(_.id === expense.id)
        .update(expenseToTuple(expense))
    )
      .flatMap { x =>
        if (x > 0) {
          Future.successful(expense)
        } else {
          Future.failed(new Exception("Nothing was updated"))
        }
      }
  }

  override def get(start: Date, end: Date, purpose: Option[String], note: Option[String]): Future[List[Expense]] = {
    val q = expenses
      .filter(_.date >= start.toInt)
      .filter(_.date <= end.toInt)
    val q1 = purpose.map(p => q.filter(_.purpose.toLowerCase === p.toLowerCase)).getOrElse(q)
    val q2 = note.map(p => q1.filter(_.note.toLowerCase like s"%${p.toLowerCase}%")).getOrElse(q1)
    db.run(q2.result).map(_.toList).map(x => x.map(tupleToExpense))
  }

  override def purposes(search: Option[String]): Future[List[String]] = {
    val map = if (search.isDefined){
      expenses
        .filter(_.purpose like s"%${search.get}%")
        .groupBy(_.purpose)
        .map(_._1)
    } else {
      expenses
        .groupBy(_.purpose)
        .map(_._1)
    }
    db.run(map.result).map(_.toList)
  }


  class DbExpenses(tag: Tag) extends Table[(Long, String, String, Int, BigDecimal)](tag, "EXPENSE_SLICK") {
    // This is the primary key column:

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def purpose = column[String]("target", O.Length(255, varying = true))

    def note = column[String]("note", O.Length(255, varying = true))

    def date = column[Int]("dateint")

    def amount = column[BigDecimal]("amount")

    override def * : ProvenShape[(Long, String, String, Int, BigDecimal)] = (id, purpose, note, date, amount)
  }

}

