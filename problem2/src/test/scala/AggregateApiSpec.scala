import aggregateservice.AggregateServiceRest
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class AggregateApiSpec extends WordSpec with Matchers with ScalatestRouteTest {
  val restService = new AggregateServiceRest

  "readme sample" should {
    "produce the same result in readme" in {
      val postData =
        """
      {
        "function": "sum", "values": "12.0, 13.00, 23.42", "valueType": "double"
      }
      """
      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/", httpEntity) ~> restService.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual ("{\"result\":{\"result\":\"48.42\"}}")
      }
    }
  }

  "Sum function" should {
    "process long values of '1000 2000 3000 4000'" in {
      val postData =
        s"""
      {
        "function": "sum", "values": "1000, 2000, 3000, 4000, ${Long.MinValue}, ${Long.MaxValue}", "valueType": "long"
      }
      """
      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/", httpEntity) ~> restService.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual ("{\"result\":{\"result\":\"9999\"}}")
      }
    }
  }

  "Max function" should {
    "process long values of '1000 2000 3000 4000 34342 323'" in {
      val postData =
        s"""
      {
        "function": "max", "values": "1000, 2000, 3000, 34342,323", "valueType": "long"
      }
      """
      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/", httpEntity) ~> restService.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual ("{\"result\":{\"result\":\"34342\"}}")
      }
    }
  }

  "invalid requests" should {
    "reject function name" in {
      val postData = """
      {
        "function": "hmm", "values": "12.0, 13.00, 23.42", "valueType": "double"
      }
      """
      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/", httpEntity) ~> restService.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual("""{"error":{"message":"Received unknown aggregate function: hmm"}}""")
      }
    }

    "reject faulty values" in {
      val postData = """
      {
        "function": "sum", "values": "12.0 2", "valueType": "double"
      }
      """
      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/", httpEntity) ~> restService.route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }


    "reject faulty valueType" in {
      val postData = """
      {
        "function": "sum", "values": "12.0 2", "valueType": "invalid"
      }
      """
      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/", httpEntity) ~> restService.route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual("""{"error":{"message":"Received unknown value type: invalid"}}""")
      }
    }
  }
}
