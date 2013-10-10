#!/bin/sh -e

lein do clean, uberjar
mv target/http*-standalone.jar httpcheck.jar
