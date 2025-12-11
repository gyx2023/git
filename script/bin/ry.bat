rem 使用者应根据自身平台编码自行转换 防止乱码 例如 win使用gbk编码
@echo off

:: 功能说明：快速部署运行脚本
:: 开发团队：CRM 开发团队
:: 官方网站：http://www.crm.vip

rem jar平级目录
set AppName=crm-admin.jar

rem JVM参数
set JVM_OPTS="-Dname=%AppName%  -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps  -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC"


ECHO.
	ECHO.  [1] 启动%AppName%
ECHO.  ======= CRM 快速部署启动脚本 =======
	ECHO.  [2] 关闭%AppName%
	ECHO.  [3] 重启%AppName%
	ECHO.  [4] 启动状态 %AppName%
	ECHO.  [5] 退 出
ECHO.

ECHO.请输入选择项目的序号:
set /p ID=
	IF "%id%"=="1" GOTO start
	IF "%id%"=="2" GOTO stop
	IF "%id%"=="3" GOTO restart
	IF "%id%"=="4" GOTO status
	IF "%id%"=="5" EXIT
PAUSE
:start
    for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if  defined pid (
		echo %%is running
		PAUSE
	)

start javaw %JVM_OPTS% -jar %AppName%

echo  starting……
echo. & echo CRM 启动成功，正在运行中...
goto:eof

rem 函数stop通过jps命令查找pid并结束进程
:stop
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo. & echo CRM 未运行) else (
		echo prepare to kill %image_name%
		echo start kill %pid% ...
		rem 根据进程ID，kill进程
		taskkill /f /pid %pid%
	)
goto:eof
:restart
	call :stop
    call :start
goto:eof
:status
	for /f "usebackq tokens=1-2" %%a in (`jps -l ^| findstr %AppName%`) do (
		set pid=%%a
		set image_name=%%b
	)
	if not defined pid (echo. & echo CRM 未运行) else (
		echo. & echo CRM 正在运行中...
	)
goto:eof
