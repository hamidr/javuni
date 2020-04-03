package typeclasses

trait Byteable[T] {
  def toBytes: T => Vector[Byte]
  def toT: Vector[Byte] => Option[T]
}

object Byteable {
  def apply[T](implicit byteable: Byteable[T]): Byteable[T] = byteable
}