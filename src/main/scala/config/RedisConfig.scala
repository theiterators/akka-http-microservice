package config

object RedisConfig extends BaseConfig {
  private val redisConfig = conf.getConfig("redis")

  val host: String = redisConfig.getString("host")
  val port: Int = redisConfig.getInt("port")
}
