pid=$(ps -ef | grep java | grep starter | awk '{print $2}')
if [ -n "$pid"  ]; then
    kill -15 "$pid"
    echo "stop $pid successful!"
fi

nohup java -Dspring.profiles.active=prod -jar starter-0.2.6.jar > console.log 2>&1 &
pid=$(ps -ef | grep java | grep starter | grep -v "$pid" | awk '{print $2}')
echo "start $pid successful!"