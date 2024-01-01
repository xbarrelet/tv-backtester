sbt -J-Xms4g -J-Xmx4g assembly &&
export SESSION_ID=bug0i5znij5vdax705j9gjo54owxeqzi &&
export CHART_ID=5H92Bwc5 &&
export CRAWLERS_NUMBER=6 &&
java -Xmx8g -jar target/scala-3.3.1/tv-backtester-assembly-0.1.0-SNAPSHOT.jar