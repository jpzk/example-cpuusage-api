package com.assignment.cpuapi

import java.io._
import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.parseFile(new File("application.conf"))
  def CSVDirectory = config.getString("csv-directory")
}
