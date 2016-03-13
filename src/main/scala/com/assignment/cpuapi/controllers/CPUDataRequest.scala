package com.assignment.cpuapi.controllers

case class CPUDataRequest(clientid: String, payloadid: String, values: List[Int], timestamps: List[Int])

