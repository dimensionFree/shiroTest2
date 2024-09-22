FROM 724758113747.dkr.ecr.ap-northeast-1.amazonaws.com/my_image_store:jdk-11.0
MAINTAINER demensionFree
LABEL app="backend" version="0.0.1" by="demensionFree"
COPY ./target/shiroTest-1.0-SNAPSHOT.jar backend.jar
CMD java -jar backend.jar