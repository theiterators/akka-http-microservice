#!/usr/bin/env sh

URL=$1

curl -X POST "http://localhost:9001/?url=$URL"