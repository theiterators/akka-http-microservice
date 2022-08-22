//import akka.actor.ActorSystem as ClassicActorSystem
//import akka.actor.typed.ActorSystem as TypedActorSystem
//import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
//import akka.event.{Logging, LoggingAdapter}
//import akka.http.scaladsl.Http
//import http.Service
//import akka.actor.typed.ActorSystem as TypedActorSystem
//import akka.actor.typed.scaladsl.Behaviors
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.*
//import akka.http.scaladsl.server.Directives.*
//import akka.http.scaladsl.server.Route
//import config.{HttpConfig,RedisConfig}
//import http.Router
//import akka.util.Timeout
//import scala.io.StdIn
//import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
//import storage.Redis
//import akka.actor.typed.{ActorRef, Behavior, Terminated}
//import id_generator.{BlockManager, IdGenerator}
//
//object ServerMain extends App {  
//  implicit val typedActorSystem: TypedActorSystem[Nothing] = TypedActorSystem(Behaviors.empty, "url-shortener")
//  implicit val classicActorSystem: ClassicActorSystem = ClassicActorSystem()
//  implicit val executor: ExecutionContextExecutor = typedActorSystem.executionContext
//  implicit val timeout: Timeout = Timeout(FiniteDuration(50, MILLISECONDS))
//
//
//  val redis: Redis = storage.Redis(RedisConfig.host, RedisConfig.port)(classicActorSystem)
//  val blockManager: ActorRef[BlockManager.Command] =
//    typedActorSystem.executionContext.spawn(BlockManager.create(redis), "blockManager")
//  val generator: ActorRef[IdGenerator.Command] =
//    context.spawn(IdGenerator.create(serverId = "1", blockManager), "idGenerator")
//  val encoder: HashidsEncoder = HashidsEncoder()
//  val shortener = MyShortener(generator, encoder, redis)
//
//  val router = Router(shortener)
//  
//  val bindingFuture = Http().newServerAt(HttpConfig.host, HttpConfig.port).bind(router)
//
//  println(s"Server now online. Please navigate to http://${HttpConfig.host}:${HttpConfig.port}")
//  println("Press RETURN to stop...")
//  StdIn.readLine() // let it run until user presses return
//  bindingFuture
//    .flatMap(_.unbind()) // trigger unbinding from the port
//    .onComplete(_ => system.terminate()) // and shutdown when done
//
//
//}
