
pid=$(ps -ef | grep java | grep MilkyDemoApplication | awk '{print $2}')
if [ -n "$pid"  ]; then
    kill -15 "$pid"
fi

# shellcheck disable=SC2046
script_path=$(cd $(dirname $0) || exit;pwd)

nohup java -cp "./bin/*:./lib/*" -Dstatic.resource="${script_path}/static/" -Dspring.profiles.active=prod com.bilanee.octopus.Application > console.log 2>&1 &
