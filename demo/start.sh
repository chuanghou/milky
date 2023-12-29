#!/bin/bash

############################################################
##
##      java program startup script for linux system
##
############################################################

############### common functions section  ##################
javaVersion(){
 version=$("$1" -version 2>&1 | awk -F '"' '/version/ {print $2}')
}

javaMajorVersion(){
 javaVersion "$1"
    version="${version%%.*}"

    if [ ${version} -eq 1 ]; then
        # Version seems starts from 1, we need second number.
        javaVersion "$1"
        backIFS=$IFS

        IFS=. ver=(${version##*-})
        version=${ver[1]}

        IFS=$backIFS
    fi
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

checkJava(){
 if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
   JAVACMD="$JAVA_HOME/jre/sh/java"
  elif [ -x "$JAVA_HOME/bin/java" ]; then
   JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ] ; then
   die " ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

 Please set the JAVA_HOME variable in your environment to match the location of your Java installation."
  fi
 else
  JAVACMD=`type -p java`
  RETCODE=$?
  if [ $RETCODE -ne 0 ]; then
   die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
 Please set the JAVA_HOME variable in your environment to match the location of your Java installation."
  fi
 fi
}

# Escape application args
save () {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done
    echo " "
}

############### variable to be replaced with truly ###################

UNIT_ID=demo

############### main script section  ##################

# Check Java environment
checkJava

javaVersion $JAVACMD

############### main script section  ##################

# Check Java environment
checkJava

javaVersion $JAVACMD

echo "Java cmd: $JAVACMD"
echo "Java version: $version"
echo "run type: $BUILD_TYPE"

# Add default JVM options here.
#堆内存的配置参考虚机内存和实际使用情况调整
DEFAULT_JVM_OPTS='"-Xms512m" "-Xmx512m" "-XX:+DisableExplicitGC"'
echo "DEFAULT_JVM_OPTS: $DEFAULT_JVM_OPTS"

APP_HOME="`pwd`"

echo "APP_HOME=$APP_HOME"

ACTIVE_ENV=""
if [ $# -lt 1 ]; then
    if [ ! $env ];
        then
        echo "please input the environment:dev, sit, uat, prod"
     exit 1
    else
     ACTIVE_ENV=$env
     echo "env=" $env
    fi
else
   ACTIVE_ENV=$1
   echo "env=$1"
fi



#竟然强制需要-D参数
APM_ARGS="-Dstatic.resource=$APP_HOME"

echo "APM_ARGS=" $APM_ARGS

#远程调试
REMOTE_DEBUGGING=""
if [ "$ACTIVE_ENV"x = "dev"x ]  || [ "$ACTIVE_ENV"x = "sit"x ]; then
    REMOTE_DEBUGGING="-Xdebug -Xrunjdwp:transport=dt_socket,address=8090,suspend=n,server=y"
fi
echo "REMOTE_DEBUGGING=" $REMOTE_DEBUGGING

LIBRA_SERVER_OPTS="$LIBRA_SERVER_OPTS -Dfile.encoding=UTF8 -Dsun.jnu.encoding=UTF8 -Dspring.profiles.active=$ACTIVE_ENV"

#当JVM发生OOM时，自动生成DUMP文件
OOM="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/"

# Collect all arguments for the java command, following the shell quoting and substitution rules
eval set -- $DEFAULT_JVM_OPTS $OOM $REMOTE_DEBUGGING $LIBRA_SERVER_OPTS $APM_ARGS -Djava.ext.dirs=\"$JAVA_HOME/lib/ext:$JAVA_HOME/jre/lib/ext\"

# kill last process
pid=$(ps -ef | grep java | grep Application | awk '{print $2}')

echo "pid is $pid"

if [ -n "$pid"  ]; then
    kill -15 "$pid"
fi

# start a new process
echo "JAVACMD=$JAVACMD"
echo "$@"
nohup $JAVACMD -cp bin/*:dependency/* $@ $APP_ARGS com.stellariver.milky.demo.MilkyDemoApplication > app.log 2>&1 &
