package examples

import akka.actor.ActorSystem
import redis.RedisClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

object RedisString extends App {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()

  val redis = RedisClient()

  val key = "test"
  val futureSet = redis.set(key, "testValue")
  val futureGet = futureSet.flatMap(r => redis.get(key))
  futureGet.map(r => {
    println(s"Redis replied with a $r")
  })
  Await.result(futureSet, Duration(5, SECONDS))

}