sbt -J-Xms4g -J-Xmx4g assembly &&
# You need to login in your browser in TV and copy the value of the cookie sessionId in your environment variables
export SESSION_ID=oj0sbjb98y8g77ubrs5ivy38tunlhm7m &&
export CRAWLERS_NUMBER=4 &&
java -Xmx8g -jar target/scala-3.5.0/tv-backtester-assembly-0.1.0-SNAPSHOT.jar
