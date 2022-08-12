package examples

import akka.actor.ActorSystem
import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

object HelloRedis extends App {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()

  val redis = RedisClient()

  val futurePong = redis.ping()
  println("Ping sent!")
  futurePong.map(pong => {
    println(s"Redis replied with a $pong")
  })
  Await.result(futurePong, Duration(5, SECONDS))

}