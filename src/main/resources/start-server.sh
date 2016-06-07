#!/bin/bash

JAVA_HOME="/usr/java/default"
JAVA_OPTS=""
HADOOP_COMMON_HOME="/usr/lib/hadoop"
HADOOP_HDFS_HOME="/usr/lib/hadoop-hdfs"
HADOOP_MAPRED_HOME="/usr/lib/hadoop-0.20-mapreduce"
CLASS="org.apache.hadoop.contrib.ftp.HdfsOverFtpServer"
JAVA_CMD="$JAVA_HOME/jre/bin/java"
CLASSPATH=".:lib/*:$HADOOP_COMMON_HOME/lib/*:$HADOOP_COMMON_HOME/*:$HADOOP_HDFS_HOME/*:$HADOOP_MAPRED_HOME/*::/etc/hadoop/conf"
OUT_LOG="hdfs-over-ftp.out"
pid=/tmp/hdfs-over-ftp.pid
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
