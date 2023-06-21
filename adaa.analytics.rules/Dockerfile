FROM ubuntu:xenial
MAINTAINER Paweł Matyszok <Pawel.Matyszok@polsl.pl>
ENV DEBIAN_FRONTEND noninteractive

ENV HOME /data
WORKDIR /data

ADD https://github.com/adaa-polsl/SCARI/releases/download/v1.0/ac-rules-1.3.6-all.jar /bin

RUN ln -s /bin/ac-rules-1.3.6-all.jar /data/ac-rules.jar

RUN apt-get update -q && apt-get install -qy openjdk-8-jre 


VOLUME ["/data"]