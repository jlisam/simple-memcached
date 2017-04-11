#!/bin/bash
DIR=$(dirname ${0})
PROJECT_ROOT="$(cd $DIR/.. >/dev/null 2>&1 && echo $PWD)"
java -jar ${PROJECT_ROOT}/build/libs/memcached-1.0-SNAPSHOT.jar $@
