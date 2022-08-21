import akka.NotUsed
import akka.actor.ActorSystem as ClassicActorSystem
import akka.actor.typed.ActorSystem as TypedActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.util.Timeout
import encoder.HashidsEncoder
import storage.Redis
import id_generator.{BlockManager, IdGenerator}
import shortener.MyShortener
import http.Router
import config.{HttpConfig, RedisConfig}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

object Main {


  def createApp(): Behavior[Nothing] =
    Behaviors.setup { context =>
      implicit val typedActorSystem: TypedActorSystem[Nothing] = context.system
      implicit val classicActorSystem: ClassicActorSystem = ClassicActorSystem()
      implicit val timeout: Timeout = Timeout(FiniteDuration(50, MILLISECONDS))


      val redis: Redis = storage.Redis(RedisConfig.host, RedisConfig.port)(classicActorSystem)
      val blockManager: ActorRef[BlockManager.Command] =
        context.spawn(BlockManager.create(redis), "blockManager")
      val generator: ActorRef[IdGenerator.Command] =
        context.spawn(IdGenerator.create(serverId = "1", blockManager), "idGenerator")
      val encoder: HashidsEncoder = HashidsEncoder()
      val shortener = MyShortener(generator, encoder, redis)

      val router = Router(shortener)
      Http()(classicActorSystem).bindAndHandle(router.routes, HttpConfig.host, HttpConfig.port)

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }

  def main(args: Array[String]): Unit = {
    TypedActorSystem(createApp(), "Url-shortener")
  }

}
