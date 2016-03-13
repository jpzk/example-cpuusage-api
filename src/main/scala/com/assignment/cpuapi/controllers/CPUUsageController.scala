package com.assignment.cpuapi.controllers

import javax.inject.{Inject, Singleton}
import com.assignment.cpuapi.services.{CouldNotWriteValues, CSVFileWriter, WarehouseService}
import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import scala.util.{Failure, Success}

@Singleton
class CPUUsageController @Inject()(client: CSVFileWriter) extends Controller with Logging {
  val warehouseService = new WarehouseService(client)

  post("/cpuusage", name="cpu_usage_create") { request: CPUDataRequest =>
    warehouseService(request).flatMap { promise =>
      val payloadId = request.payloadid
      val ok = s"{'status':'ok', 'payloadid':'$payloadId'}"
      val resend = s"{'status':'resend', 'payloadid':'$payloadId'}"

      promise match {
        case Success(result) => { response.ok.json(ok).toFuture}
        case Failure(e: CouldNotWriteValues) =>
          warn("Could not write values; ask client for resending")
          response.ok.json(resend).toFuture
        case Failure(e) =>
          warn("Some unexpected error when writing values")
          response.ok.json(resend).toFuture
      }
    }
  }
}

