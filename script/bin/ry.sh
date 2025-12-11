#!/bin/sh
# ./ry.sh start 启动 stop 停止 restart 重启 status 状态
# 功能说明：快速部署运行脚本
# 开发团队：CRM 开发团队
# 官方网站：http://www.crm.vip
AppName=crm-admin.jar

# JVM参数
JVM_OPTS="-Dname=$AppName  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"
APP_HOME=`pwd`
LOG_PATH=$APP_HOME/logs/$AppName.log

if [ "$1" = "" ];
then
    echo -e "\033[0;31m 未输入操作名 \033[0m  \033[0;34m {start|stop|restart|status} \033[0m"
    exit 1
fi

if [ "$AppName" = "" ];
then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi

function start()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`

    if [ x"$PID" != x"" ]; then
        echo "CRM 正在运行中..."
    else
        nohup java $JVM_OPTS -jar $AppName > /dev/null 2>&1 &
        echo "CRM 启动成功，正在运行中..."
    fi
}

function stop()
{
    echo "停止 CRM"

    PID=""
    query(){
        PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`
    }

    query
    if [ x"$PID" != x"" ]; then
        kill -TERM $PID
        echo "CRM (pid:$PID) 正在退出..."
        while [ x"$PID" != x"" ]
        do
            sleep 1
            query
        done
        echo "CRM 已停止运行"
    else
        echo "CRM 已经停止运行"
    fi
}

function restart()
{
    stop
    sleep 2
    start
}

function status()
{
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|wc -l`
    if [ $PID != 0 ];then
        echo "CRM 正在运行中..."
    else
        echo "CRM 未运行"
    fi
}

case $1 in
    start)
    start;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
    *)

esac
