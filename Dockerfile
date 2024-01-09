FROM mcr.microsoft.com/playwright:next-jammy-arm64

RUN wget https://download.oracle.com/java/21/latest/jdk-21_linux-aarch64_bin.rpm && \
    rpm -i jdk-21_linux-aarch64_bin.rpm && \
    rm jdk-21_linux-aarch64_bin.rpm

ADD target/scala-3.3.1/tv-backtester-assembly-0.1.0-SNAPSHOT.jar /tv-backtester.jar

ENV SESSION_ID bug0i5znij5vdax705j9gjo54owxeqzi
ENV CHART_ID=5H92Bwc5
ENV CRAWLERS_NUMBER=4

ENTRYPOINT ["java","-Xmx4g", "-jar","/tv-backtester.jar"]

