package models

final case class ProcessedData(
  name: SensorType,
  average: Option[Double],
  median: Option[Double],
  min: Option[Double],
  max: Option[Double]
)

object ProcessedData {
  def fromMap(name: SensorType, fields: Map[OpFunc, Double]): ProcessedData =
    ProcessedData(
      name = name,
      max = fields.get(Max),
      min = fields.get(Min),
      average = fields.get(Average),
      median = fields.get(Median)
    )

}