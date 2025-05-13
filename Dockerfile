FROM ubuntu:latest
LABEL authors="kayky"

ENTRYPOINT ["top", "-b"]