docker build -t tv-backtester . &&
docker tag tv-backtester 192.168.1.56:5000/tv-backtester
docker push 192.168.1.56:5000/tv-backtester