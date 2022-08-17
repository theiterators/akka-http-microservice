#!/bin/bash

docker stop redis
docker rm redis
echo "redis stopped"

docker run -d -p 6379:6379 --name redis -v redis_data:/data redis:7-alpine \
  --save 60 1 --loglevel warning
echo "redis started"


