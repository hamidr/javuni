package models

import java.nio.file.Path

import cats.effect.Sync
import pureconfig.{CamelCase, ConfigFieldMapping}
import pureconfig.module.catseffect._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

final case class AppConfig(
  ipAddress: String,
  port: Int,
  dir: Path,
  filteringRules: Set[String],
  readChunkSize: Int,
  processChunkSize: Int
)

object AppConfig {
  implicit def hint[T]: ProductHint[T] =
    ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase), allowUnknownKeys = true)

  def load[F[_]: Sync]: F[AppConfig] = loadConfigF[F, AppConfig]
}

