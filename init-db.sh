#!/usr/bin/env bash
docker run --name "postgis" -e POSTGRES_USER="dbuser" -e POSTGRES_PASS="dbpass" -e POSTGRES_DBNAME="dbtest" -p 25432:5432 l --restart=always -d -t kartoza/postgis
