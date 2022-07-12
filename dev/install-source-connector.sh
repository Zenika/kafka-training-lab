#!/usr/bin/env bash

curl -XPOST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
   http://localhost:8083/connectors \
   --data @connector-source.json
