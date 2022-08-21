package config

object HttpConfig extends BaseConfig {
  private val httpConfig = conf.getConfig("http")
  val host: String = httpConfig.getString("interface")
  val port: Int = httpConfig.getInt("port")
}

