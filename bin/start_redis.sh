#!/bin/bash

docker run -d -p 6379:6379 --name redis -v redis_data:/data redis:7-alpine --save 60 1 --loglevel warning