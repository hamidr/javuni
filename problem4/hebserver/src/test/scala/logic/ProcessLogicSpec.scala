package logic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import fs2.Stream
import logics.ProcessLogic
import cats.effect.unsafe.implicits.global
import models._

import scala.util.Random

//import org.http4s.multipart.{Multipart, Part}

class ProcessLogicSpec extends AnyFlatSpec with Matchers  {

  val sample = Seq(7.28,96.08,87.41,12.88,89.59,10.32,4.29,27.64,96.39,24.21,55.97,63.68,
    30.56,49.48,25.42,94.45,80.67,43.82,5.45,84.06)

  it should "Find Maximum of sample in normal order" in {
    val compute = Stream.emits(sample).through(ProcessLogic.fold2Max).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Max -> 96.39)
    compute.unsafeRunSync() shouldNot equal(Map(Max -> 92.39))
  }

  it should "Find Maximum of sample in randomized order" in {
    val sample2 = Random.shuffle(sample)
    val compute = Stream.emits(sample2).through(ProcessLogic.fold2Max).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Max -> 96.39)
    compute.unsafeRunSync() shouldNot equal(Map(Max -> 92.39))
  }

  it should "Find Minimum of sample in normal order" in {
    val compute = Stream.emits(sample).through(ProcessLogic.fold2Min).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Min -> 4.29)
    compute.unsafeRunSync() shouldNot equal(Map(Min -> 92.39))
  }

  it should "Find Minimum of sample in randomized order" in {
    val sample2 = Random.shuffle(sample)
    val compute = Stream.emits(sample2).through(ProcessLogic.fold2Min).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Min -> 4.29)
    compute.unsafeRunSync() shouldNot equal(Map(Min -> 92.39))
  }

  it should "Find Average of sample in normal order" in {
    val compute = Stream.emits(sample).through(ProcessLogic.fold2Average)
      .map(_.view.mapValues(d => f"$d%1.4f").toMap).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Average -> "49.4825")
    compute.unsafeRunSync() shouldNot equal(Map(Average -> 92.39))
  }

  it should "Find Average of sample in randomized order" in {
    val sample2 = Random.shuffle(sample)
    val compute = Stream.emits(sample2).through(ProcessLogic.fold2Average)
      .map(_.view.mapValues(d => f"$d%1.4f").toMap).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Average -> "49.4825")
    compute.unsafeRunSync() shouldNot equal(Map(Average -> 92))
  }

  it should "Find Median of sample in normal order" in {
    val compute = Stream.emits(sample).through(ProcessLogic.fold2Median(3)).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Median -> 40.09)
    compute.unsafeRunSync() shouldNot equal(Map(Average -> 92.39))
  }

  it should "Find Median of a large dataset" in {
    val compute = Stream.emits(sample).repeat.take(99999)
      .through(ProcessLogic.fold2Median(99)).compile.lastOrError
    compute.unsafeRunSync() shouldBe Map(Median -> 40.09)
    compute.unsafeRunSync() shouldNot equal(Map(Average -> 92.39))
  }
}