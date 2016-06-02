#!/bin/bash

JAVA_HOME="/usr/java/jdk1.7.0_51"
JAVA_OPTS="-Xmx8192m"
HADOOP_CONF_HOME="/etc/hadoop/conf"
HADOOP_COMMON_HOME="/usr/lib/hadoop"
HADOOP_HDFS_HOME="/usr/lib/hadoop-hdfs"
HADOOP_MAPRED_HOME="/usr/lib/hadoop-0.20-mapreduce"
CLASS="org.apache.hadoop.contrib.ftp.HdfsOverFtpServer"
JAVA_CMD="$JAVA_HOME/jre/bin/java"
CLASSPATH=".:lib/*:$HADOOP_COMMON_HOME/lib/*:$HADOOP_COMMON_HOME/*:$HADOOP_HDFS_HOME/*:$HADOOP_MAPRED_HOME/*:$HADOOP_CONF_HOME"
OUT_LOG="/var/log/hdfs-over-ftp/hdfs-over-ftp.out"
pid=/var/run/hdfs-over-ftp/hdfs-over-ftp.pid
command="hdfs-over-ftp"
usage="Usage: hdfs-over-ftp.sh (start|stop)"
cmd=start

case $cmd in

  (start)

    if [ -f $pid ]; then
        echo $command running as process `cat $pid`. Stop it first.
        exit 1
    fi

    echo starting $command
      $JAVA_CMD ${JAVA_OPTS} -cp ${CLASSPATH} ${CLASS} &> $OUT_LOG & echo $! > $pid
    ;;

  (*)
    echo $usage
    exit 1
    ;;
esac
