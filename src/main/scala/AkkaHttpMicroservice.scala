import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import expenses.repo.{ExpensesRepo, SlickExpensesRepo}
import expenses.{Date, Expense}
import slick.driver.{MySQLDriver, H2Driver}
import slick.jdbc.JdbcBackend.Database
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}


trait Protocols extends DefaultJsonProtocol {
  implicit val dateFormat = jsonFormat2(Date.apply)
  implicit val expenseFormat = jsonFormat5(Expense.apply)
}

trait Service extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  def config: Config

  val logger: LoggingAdapter
  val expensesRepo: ExpensesRepo

  val routes = {
//    logRequestResult("akka-http-microservice") {
      pathPrefix("expenses") {
        (put & entity(as[Expense])) { e =>
          complete {
            expensesRepo.add(e).map[ToResponseMarshallable](x => x)
          }
        } ~
          (get & path("id" / Segment)) {
            date =>
              complete {
                val eventualExpenses: Future[List[Expense]] = expensesRepo.get(Date(date.split('-')(0).toInt, date.split('-')(1).toInt))
                eventualExpenses.map[ToResponseMarshallable](x => x)
              }
          } ~
          (delete & path("id" / LongNumber)) { id =>
            complete {
              expensesRepo.delete(id).map(i => s"Removed $i expenses")
            }
          } ~
          (post & path("id" / LongNumber)) {
            id =>
              entity(as[Expense]) {
                expense =>
                  if (expense.id.contains(id)) {
                    complete {
                      expensesRepo.update(expense)
                    }
                  } else {
                    reject
                  }
              }
          }
      } ~ pathPrefix("purposes" / Segments(0, 1)) {
        purpose =>
          get {
            val a: List[String] = purpose
            complete {
              val r = expensesRepo.purposes(purpose.headOption)
              r.map[ToResponseMarshallable](x => x)
            }
          }
      } ~ pathPrefix("search" / "from" / Segment / "to" / Segment) {
        (from, to) =>
          val a = Date(from)
          val b = Date(to)
          path("purpose" / Segment / "note" / Segment) {
            (purpose, note) =>
              get {
                complete {
                  val r = expensesRepo.get(a, b, Some(purpose), Some(note))
                  r.map[ToResponseMarshallable](x => x)
                }
              }
          } ~ path("note" / Segment / "purpose" / Segment) {
            (note, purpose) =>
              get {
                complete {
                  val r = expensesRepo.get(a, b, Some(purpose), Some(note))
                  r.map[ToResponseMarshallable](x => x)
                }
              }
          } ~
            path("purpose" / Segment) {
              purpose =>
                get {
                  complete {
                    val r = expensesRepo.get(a, b, Some(purpose), None)
                    r.map[ToResponseMarshallable](x => x)
                  }
                }
            } ~
            path("note" / Segment) {
              note =>
                get {
                  complete {
                    val r = expensesRepo.get(a, b, None, Some(note))
                    r.map[ToResponseMarshallable](x => x)
                  }
                }
            } ~
            get {
              complete {
                val r = expensesRepo.get(a, b, None, None)
                r.map[ToResponseMarshallable](x => x)
              }
            }
//      }
    }

  }
}


object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  val db = Database.forConfig("mysqlDB")
  private val driver: MySQLDriver.type = MySQLDriver
  override val expensesRepo = new SlickExpensesRepo(driver, db)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
