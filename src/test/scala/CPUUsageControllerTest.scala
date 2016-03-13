import com.assignment.cpuapi.controllers.CPUDataRequest
import com.assignment.cpuapi.services.{CSVWriterResponse, CSVFileWriter}
import com.assignment.cpuapi.CPUUsageServer
import com.google.inject.testing.fieldbinder.Bind
import com.twitter.finatra.http.test.{HttpTest, EmbeddedHttpServer}
import com.twitter.finagle.http.Status
import com.twitter.inject.server.FeatureTest
import com.twitter.inject.Mockito
import scala.util.{Failure, Success}

/**
  * Feature test of the com.assignment.cpuapi.controllers.CPUUsageController
  */
class CPUUsageControllerTest extends FeatureTest with Mockito with HttpTest {
  override val server = new EmbeddedHttpServer(new CPUUsageServer)
  @Bind val writer = smartMock[CSVFileWriter]

  val validObject = CPUDataRequest("test-client", "1", List(1, 2, 3), List(0, 0, 0))
  val validPayload =
    """
      {
        "clientid":"test-client",
        "payloadid":"1",
        "values":[1,2,3],
        "timestamps":[0,0,0]
        }
    """.stripMargin
  val invalidPayload =
  """
    {
      "clientid":"test-client",
      "values":[1,2,3],
      "timestamps":[0,0,0]
    }
  """.stripMargin

  "Posting CPU values with missing values" in {
      writer.addValues(validObject) returns Success(CSVWriterResponse())
      val response = server.httpPost(
        path = "/cpuusage",
        postBody = invalidPayload, andExpect = Status.BadRequest)
  }

  "Posting CPU values with valid write" in {
    writer.addValues(validObject) returns Success(CSVWriterResponse())
    val response = server.httpPost(
      path = "/cpuusage",
      postBody = validPayload, andExpect = Status.Ok)
    assert(response.contentString == "{'status':'ok', 'payloadid':'1'}")
  }

  "Posting CPU values without valid write" in {
    writer.addValues(validObject) returns Failure(new Exception())
    val response = server.httpPost(
      path = "/cpuusage",
      postBody = validPayload, andExpect = Status.Ok)
    assert(response.contentString == "{'status':'resend', 'payloadid':'1'}")
  }
}
