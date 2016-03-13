#!/bin/env python
# Python test / benchmark client for CPU usage API

import requests
import uuid
import time
import random
import json

api_endpoint = "http://localhost:8888/cpuusage"
payloadsGenerated = 0
clients = 100
measures_per_payload = 10
time_period = 10 # 10 seconds, 100 measures
runs = 10

def generateCpuUsage(): return int(random.random() * 100)
def getTimestamp(): return int(time.time())
def generateCpuId(client): return str(uuid.uuid1(uuid.getnode(), client))

clientIds = map(lambda x: generateCpuId(x), range(clients))
for run in range(runs):

  # each client sends a payload every 10 seconds
  for client in clientIds:
    values = map(lambda i : generateCpuUsage(), range(measures_per_payload))
    timestamps = map(lambda i : getTimestamp() + 1000 * i, range(measures_per_payload))
    payload = {'clientid': client, 'payloadid': payloadsGenerated,
        'values': values, 'timestamps': timestamps}   
    print json.dumps(payload)
    r = requests.post(api_endpoint, json=payload)
    payloadsGenerated = payloadsGenerated + 1
    print r.text
  time.sleep(10)
