import cats.effect.IO
import models.Database
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import pureconfig._

final case class AppConfig(
  ipAddress: String,
  port: Int,
  processingLimit: Int,
  cassandra: Option[Database]
)

object AppConfig {
  implicit def hint[T]: ProductHint[T] =
    ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase), allowUnknownKeys = true)

  def load: IO[AppConfig] = loadConfigF[IO, AppConfig]
}