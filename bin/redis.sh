#!/bin/bash

start() {
  docker rm redis
  docker run -d -p 6379:6379 --name redis -v redis_data:/data redis:7-alpine \
    --save 60 1 --loglevel warning
  echo "redis started"
}

stop() {
  docker stop redis
  echo "redis stopped"
}

case "$1" in
    'start')
            start
            ;;
    'stop')
            stop
            ;;
    'restart')
            stop ; echo "Sleeping..."; sleep 1 ;
            start
            ;;
           'status')
                      status
                      ;;
    *)
            echo
            echo "Usage: $0 { start | stop | restart | status }"
            echo
            exit 1
            ;;
esac










