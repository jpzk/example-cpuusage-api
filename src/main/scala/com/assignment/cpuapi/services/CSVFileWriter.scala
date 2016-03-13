package com.assignment.cpuapi.services

import java.io._
import javax.inject.Singleton
import com.assignment.cpuapi.Config
import com.assignment.cpuapi.controllers.CPUDataRequest
import com.github.tototoshi.csv.CSVWriter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.{Failure, Success, Try}

case class CSVWriterResponse()

@Singleton
class CSVFileWriter {
  val hour = DateTime.now().hourOfDay()
  var writer = openWriter()

  /**
    * Write the CSV header in new file
    *
    * @param writer
    */
  def writeHeader(writer: CSVWriter) = {
    writer.writeRow(Seq("timestamp","clientid","payloadid","cpuusage"))
  }

  /**
    * Open the file for a given hour
    *
    * @return
    */
  def openWriter() = {
    val fmt = DateTimeFormat.forPattern("yyyyMMdd-hh")
    val time = DateTime.now().toString(fmt)
    val dir = Config.CSVDirectory
    val writer = CSVWriter.open(new File(s"$dir/cpuusage-$time.csv"))
    writeHeader(writer)
    writer
  }

  /**
    * Adds the values to the CSV file of given hour, if raised exceptions
    * sends back Failure, this will result into resending on the client side
    *
    * @param cPUDataRequest
    * @return
    */
  def addValues(cPUDataRequest: CPUDataRequest): Try[CSVWriterResponse] = {
    try {
      if (DateTime.now().hourOfDay() != hour) {
        // a) start Spark job to aggregate the data, read from disk
        // b) send the batched data to Kafka -> Spark streaming, Spark batch

        writer.close()
        writer = openWriter()
      }
      val values = cPUDataRequest.timestamps zip cPUDataRequest.values
      val cid = cPUDataRequest.clientid
      val pid = cPUDataRequest.payloadid
      values.foreach { case (timestamp, value) =>
        writer.writeRow(Seq(timestamp, cid, pid, value))
      }
      writer.flush()
    } catch { case t: Throwable =>
      return Failure(t)
    }
    Success(CSVWriterResponse())
  }
}
