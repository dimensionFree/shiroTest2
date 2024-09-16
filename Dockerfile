FROM 190547127216.dkr.ecr.us-west-2.amazonaws.com/jdk11:latest
MAINTAINER demensionFree
LABEL app="backend" version="0.0.1" by="demensionFree"
COPY ./target/shiroTest-1.0-SNAPSHOT.jar backend.jar
CMD java -jar backend.jar