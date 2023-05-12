# Start the base container as Java 8
FROM openjdk:8
COPY --from=python:3.7 / /
COPY --from=node:18.16.0 / /

RUN npm install -g node-gyp

# Make the default shell as bash
RUN echo "dash dash/sh boolean false" | debconf-set-selections
RUN DEBIAN_FRONTEND=noninteractive dpkg-reconfigure dash
ENV ENV ~/.profile

# Install zip and unzip packages
RUN apt-get update -y && apt-get install -y zip
RUN apt-get update -y && apt-get install -y unzip

# Install sdkman and then install Java 11 and Java 8
RUN curl -s "https://get.sdkman.io" | bash

RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && yes | sdk install java 11.0.17-librca"
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && yes | sdk install java 8.0.352-librca"

#Install Maven and Graphviz
RUN apt-get update -y && apt-get install -y maven
RUN apt-get update -y && apt-get install -y graphviz

# Copy the requires resources from the host avighna folder to the container /root/avighna/ folder
ADD avighna-essential-resources/ /root/avighna/

# give permissions to jar files and create necessary directories
WORKDIR /root/avighna/
RUN chmod 755 /root/avighna/avighna-agent-1.0.0.jar
RUN chmod 755 /root/avighna/avighna-cmd-interface-1.0.0.jar
RUN mkdir Spring-Projects
RUN mkdir Reflection-Projects
RUN mkdir Guice-Projects

# Start with Spring projects
WORKDIR /root/avighna/Spring-Projects

# Install Fredbet project
RUN git clone https://github.com/ranjithkris/fredbet.git
WORKDIR /root/avighna/Spring-Projects/fredbet/
RUN git checkout -b tags_release_2.0.0 tags/release_2.0.0
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk use java 8.0.352-librca && mvn clean install -DskipTests"

# Install Spring Initializer project
WORKDIR /root/avighna/Spring-Projects
RUN git clone https://github.com/ranjithkris/start.spring.io.git
WORKDIR /root/avighna/Spring-Projects/start.spring.io/
RUN git checkout 6c0944ead1bfef3444a9e1a422b3f134de2b7f48
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk use java 11.0.17-librca && mvn clean install -DskipTests"

# Install Spring Zipkin project
WORKDIR /root/avighna/Spring-Projects
RUN git clone https://github.com/ranjithkris/zipkin.git
WORKDIR /root/avighna/Spring-Projects/zipkin/
RUN git checkout 8a4f4b9c9a5a3204d9663ecef39d687785369c9a
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk use java 8.0.352-librca && mvn clean install -DskipTests"
RUN git clone https://github.com/ranjithkris/pyramid_zipkin-example.git
WORKDIR /root/avighna/Spring-Projects/zipkin/pyramid_zipkin-example/
RUN pip install pyramid_zipkin -U
RUN python3 setup.py install

# Install CGBench project
WORKDIR /root/avighna/Spring-Projects
RUN git clone https://github.com/ranjithkris/CGBench.git
WORKDIR /root/avighna/Spring-Projects/CGBench/
RUN git checkout origin/for-avighna
# RUN git checkout 65820234b5b9a6f65bd69d78570a3caedee7f1a1
RUN rm -r guice-credentials/
RUN rm -r music-store/
RUN rm -r onlinechat/
RUN rm -r onlineshop/
RUN rm -r teleforum/
RUN rm -r webgoat/
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk use java 8.0.352-librca && python3 buildAll.py"
RUN mv guice/ /root/avighna/Guice-Projects/

# Install Spring Petclinic project
WORKDIR /root/avighna/Guice-Projects
RUN git clone https://github.com/ranjithkris/streamflow.git
WORKDIR /root/avighna/Guice-Projects/streamflow/
RUN git checkout -b tags_0.12.0 tags/0.12.0
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk use java 8.0.352-librca && mvn clean install -DskipTests"

RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk use java 8.0.352-librca"