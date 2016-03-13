package com.assignment.cpuapi.services

import javax.inject.{Inject, Singleton}
import com.assignment.cpuapi.controllers.CPUDataRequest
import com.twitter.finagle.Service
import com.twitter.inject.Logging
import com.twitter.util.Future

import scala.util.{Failure, Success, Try}

/**
  * Service used to store the cpu usage data in the warehouse
  */
@Singleton
class WarehouseService @Inject()(client: CSVFileWriter) extends
  Service[CPUDataRequest, Try[WarehouseResponse]] with Logging {

  override def apply(r: CPUDataRequest): Future[Try[WarehouseResponse]] = {
    // Optional: Also pass the CPU values to other services
    val outcome = client.addValues(r) match {
      case Failure(e: Throwable) => {
        Failure(new CouldNotWriteValues())
      }
      case Success(clientResponse) =>
        val response = WarehouseResponse(r.clientid, r.payloadid)
        Success(response)
    }
    Future.value(outcome)
  }
}
