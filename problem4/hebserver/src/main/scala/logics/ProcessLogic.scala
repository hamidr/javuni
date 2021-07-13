package logics

import cats.effect.IO
import fs2.{Chunk, Pipe, Stream}
import models._
import repositories.IRecordRepo

import scala.util.Try

class ProcessLogic(processingWindow: Int, repo: IRecordRepo) extends IProcessLogic {
  import ProcessLogic._
  def create(record: IoTRecord): IO[Unit] = repo.store(record)

  def retrieve(request: ReadingsQuery): IO[List[ProcessedData]] = {
    val operations = request.operations
      .map(opFuncToProcess(processingWindow))

    repo.retrieveRange(request.from, request.to)
      .through(processAllDevices(request.sensors, operations))
      .compile.toList
  }
}

object ProcessLogic {
  /*
  * I COULD pass the computation to the Database, however it didn't feel right if I did so...
  * Obviously Max/Min/AVG are implemented in many databases... .
  * https://docs.datastax.com/en/dse/5.1/cql/cql/cql_reference/cqlAggregates.html
  * https://www.postgresql.org/docs/current/sql-expressions.html#SYNTAX-AGGREGATES
  *
  * About Median operation:
  * Median computation requires knowledge about the Size of the Data sample.
  * Computing median of a dataset in a distributed system is cumbersome
  * I Looked for a solution and my own idea was based on currently implemented algorithm which it's accuracy is
  * questionable but still as I learned, almost all of the other algorithms has their some trade offs.
  * There needs to be enough research about the nature of stored data and the expectations of the user.
  * And that's why I gave up to go any further with my design.
  * https://wiki.postgresql.org/wiki/Aggregate_Median
  * https://leafo.net/guides/postgresql-calculating-percentile.html
  */
  type LogicProcess = Pipe[IO, Double, Fields]
  type Fields = Map[OpFunc, Double]

  def fold2Max: Pipe[IO, Double, Fields] =
    _.fold1(math.max(_,_)).map(d => Map(Max -> d))

  def fold2Min: Pipe[IO, Double, Fields] =
    _.fold1(math.min(_,_)).map(d => Map(Min -> d))

  def accumulate: ((Double, Long), (Double, Long)) => (Double, Long) = {
    case ((acc, count), (elem, _)) => ((acc + elem), (count + 1))
  }

  def fold2Average: Pipe[IO, Double, Fields] =
    _.zip(Stream.emit(1.toLong).repeat)
      .fold1(accumulate)
      .map { case (acc, size) => (acc / size) }
      .map(d => Map(Average -> d))

  def pickMedian: Chunk[Double] => Try[Double] = d => Try {
    val size = d.size
    val midNth = (size / 2)

    if (size % 2 == 0) (d(midNth) + d(midNth - 1)) / 2
    else d(midNth)
  }

  // Recursively find the median of all of chunks until the last one.
  // However it seems, there is a limitation to constructing a Stream from a recursive Stream.
  def medianOfEachChunk(portion: Int): Pipe[IO, Double, Double] =
    _.chunkMin(portion, allowFewerTotal = true) //Aggregate this much of a portion
      .map(pickMedian.andThen(_.toOption)) //pick the median of it
      .unNone //if any exception happened just ignore it
     /*.through(medianOfEachChunk(portion))*/


  /*
   * As you can see, it cuts the stream in the predefined chunks(read array) and then compute the median of them
   * and later on computes the median of those chunks.
   */
  def fold2Median(chunkLimit: Int): Pipe[IO, Double, Fields] =
    _.through(medianOfEachChunk(chunkLimit)) //Cut it in chunks
      .through(medianOfEachChunk(chunkLimit)) //Compute the result
      .last
      .unNoneTerminate
      .map(d => Map(Median -> d))

  def opFuncToProcess(processingWindow: Int): OpFunc => LogicProcess = {
    case Max => fold2Max
    case Min => fold2Min
    case Median => fold2Median(processingWindow)
    case Average => fold2Average
  }

  def recordsProcess(operations: Set[LogicProcess]): Pipe[IO, Double, Fields] =
    _.broadcastThrough(operations.toSeq:_*) //process the numbers for each computation
      .fold1 (_ ++ _) //merge the fields to a map and use the map to construct ProcessedData type

  def IoTRecord2Field(device: SensorType): IoTRecord => Double = record =>
    device match {
      case CarFuel => record.carFuel
      case Thermostat => record.thermostat
      case HeartRate => record.heartRate
    }

  def processForDevice(operations: Set[LogicProcess], device: SensorType): Pipe[IO, IoTRecord, ProcessedData] =
    _.map(IoTRecord2Field(device))
      .through(recordsProcess(operations))
      .map(ProcessedData.fromMap(device, _))

  def processAllDevices(sensors: Set[SensorType], operations: Set[LogicProcess]): Pipe[IO, IoTRecord, ProcessedData] = {
    recordStream =>
      val processingStreams = sensors.map(s => recordStream.through(processForDevice(operations, s))).toSeq
      processingStreams.foldLeft(Stream[IO, ProcessedData]())((acc, e) => acc.merge(e))
  }

  def create(processingWindow: Int, repo: IRecordRepo): IProcessLogic =
    new ProcessLogic(processingWindow, repo)
}
