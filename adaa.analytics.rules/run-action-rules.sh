#!/bin/bash
IMAGE=actionrules:latest
exec docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v `pwd -W`:/data "$IMAGE" java -jar //bin/ac-rules-1.3.6-all.jar "$@"