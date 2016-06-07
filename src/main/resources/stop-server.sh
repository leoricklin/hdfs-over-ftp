#!/bin/sh

#JAVA_HOME="/home/agladyshev/opt/jdk"
JAVA_OPTS=""
CLASS="org.apache.hadoop.contrib.ftp.HdfsOverFtpServer"
JAVA_CMD="$JAVA_HOME/jre/bin/java"
OUT_LOG="hdfs-over-ftp.out"

pid=/tmp/hdfs-over-ftp.pid

command="hdfs-over-ftp"
usage="Usage: hdfs-over-ftp.sh (start|stop)"
cmd=stop

case $cmd in

  (stop)

    if [ -f $pid ]; then
        echo stopping $command
        kill `cat $pid`
    	rm $pid
      else
        echo no $command to stop
      fi
    ;;

  (*)
    echo $usage
    exit 1
    ;;
esac
