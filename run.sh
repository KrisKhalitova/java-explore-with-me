#!/bin/bash

mvn clean install  -Dmaven.test.skip

docker container rm ewm-service
docker container rm stats-server
docker container rm stats-db
docker container rm ewm-db

docker image rm stats-server
docker image rm ewm-service
docker image rm postgres:14-alpine

docker-compose up
