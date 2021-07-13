package repositories

import cats.effect.{IO, Resource}
import models._
import fs2.Stream
import com.ringcentral.cassandra4io.CassandraSession
import com.datastax.oss.driver.api.core.{ConsistencyLevel, CqlSession}
import com.ringcentral.cassandra4io.cql._
import com.ringcentral.cassandra4io.cql.Reads.caseClassParser

import java.net.InetSocketAddress
import java.time.Instant

final case class InvalidCategoryInput(category: String)
  extends Exception(s"Invalid category as input: $category")

final case class QueryFailed(id: String, t: String)
  extends Exception(s"$t Query was not applied for $id")

class CassandraRecordRepo(session: CassandraSession[IO]) extends IRecordRepo {
  def store: IoTRecord => IO[Unit] = {
    case IoTRecord(id, thermostat, heartRate, carFuel, time) =>
      val query = cql"INSERT INTO Records (id, thermostat, heartRate, carFuel, time) VALUES($id, $thermostat, $heartRate, $carFuel, $time)"
        .config(_.setConsistencyLevel(ConsistencyLevel.ALL))
      query.execute(session)
        .flatMap(wasApplied => IO.raiseWhen(!wasApplied)(QueryFailed(id, "INSERT")))
  }

  def retrieveRange(from: Instant, to: Instant): Stream[IO, IoTRecord] = {
    val query =
      cql"""SELECT id, thermostat, heartRate, carFuel, time FROM records
              WHERE time >= $from AND
                    time < $to
              ALLOW FILTERING""".as[IoTRecord] //Used allow filtering because I haven't optimized the Table.
    query.select(session)
  }
}

object CassandraRecordRepo {
  def build(cfg: Database): Resource[IO, CassandraRecordRepo] = {
    val builder = CqlSession
      .builder()
      .addContactPoint(new InetSocketAddress(cfg.host, cfg.port))
      .withLocalDatacenter(cfg.dataCenter)
      .withKeyspace(cfg.keySpace)

    CassandraSession.connect[IO](builder).map(new CassandraRecordRepo(_))
  }
}
