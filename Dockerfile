FROM mcr.microsoft.com/playwright:v1.40.0-jammy

RUN wget https://download.oracle.com/java/21/archive/jdk-21_linux-x64_bin.deb && sudo dpkg -i jdk-21_linux-x64_bin.deb

ADD target/scala-3.3.1/tv-backtester-assembly-0.1.0-SNAPSHOT.jar /tv-backtester.jar

ENV SESSION_ID bug0i5znij5vdax705j9gjo54owxeqzi
ENV CHART_ID=5H92Bwc5
ENV CRAWLERS_NUMBER=4

ENTRYPOINT ["java","-Xmx4g", "-jar","/tv-backtester.jar"]

