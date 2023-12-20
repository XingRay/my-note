在ubuntu上简单部署java项目



1. 在ubuntu上安装java/maven/git
2. 在ubuntu上安装项目需要的组件，如 mysql/redis/mongo/es/rabbitmq/nacos/seata/sentinel ...



第一次运行执行初始化脚本，脚本自动通过git下载源码并打包运行

```bash
#!/bin/sh
echo =================================
echo  quant-platform 自动化部署脚本 初始化项目
echo =================================

echo 创建项目源码目录
mkdir code

echo 创建log目录
mkdir log

echo 创建pid目录
mkdir pid

echo 开始下载项目源码
cd code

echo 下载项目源码 quant-platform
git clone git@gitee.com:leixing1012/quant-platform.git

echo 下载项目依赖源码 finance-data
git clone git@gitee.com:leixing1012/finance-data.git

echo 下载项目依赖源码 java-common
git clone git@github.com:XingRay/java-common.git

echo 下载项目依赖源码 finance-analysis
git clone git@github.com:XingRay/finance-analysis.git

cd ..
echo 下载项目依赖源码完成

echo 开始将依赖安装到本地maven仓库
cd code

echo 将依赖安装到本地maven仓库：java-common
cd java-common
mvn -Dmaven.test.skip=true clean install
cd ..

echo 将依赖安装到本地maven仓库：finance-analysis
cd finance-analysis
mvn -Dmaven.test.skip=true clean install
cd ..

echo 将依赖安装到本地maven仓库：finance-data
cd finance-data
mvn -Dmaven.test.skip=true clean install
cd ..

cd ..
echo 开始将依赖安装到本地maven仓库完成

echo 将项目打包
cd code

cd quant-platform
mvn -Dmaven.test.skip=true clean package
cd ..

cd ..
echo 将项目打包完成

echo 项目启动

startModule()
{
	MODULE_NAME=$1;
	nohup java -Denv=prod -jar ./code/quant-platform/${MODULE_NAME}/target/${MODULE_NAME}-1.0.0.jar >./log/${MODULE_NAME}.log  2>&1 & echo $! > ./pid/${MODULE_NAME}.pid
}

echo "启动项目模块：quant-platform-gateway"
startModule quant-platform-gateway

echo "启动项目模块：quant-platform-user"
startModule quant-platform-user

echo "启动项目模块：quant-platform-fetch"
startModule quant-platform-fetch

echo "启动项目模块：quant-platform-stock"
startModule quant-platform-stock

echo 项目启动完成
```



后续代码修改了，提交到git仓库后再执行更新脚本

```bash
#!/bin/sh
echo =================================
echo  quant-platform 自动化部署脚本 更新项目
echo =================================

stopProcess()
{
	APP_NAME=$1;
	if [ -e  ./pid/${APP_NAME}.pid ]; then
		PID=`cat ./pid/${APP_NAME}.pid`
		echo 文件中记录的pid为 ${PID}
	else
		echo 没有找到记录PID的文件
	fi
	
	if [ ${PID} ];
	then
		echo "正在停止进程，PID：$PID"
		if ps -p ${PID} > /dev/null
		then
			echo "${PID}  is running"
			
			echo "kill -15 ${PID} "
			kill -15 ${PID} 
			sleep 2
			
			if ps -p ${PID}  > /dev/null
			then
				echo "stop failed"
			else
				echo 'stop Success!'
				return 0;
			fi

			echo "kill -9 ${PID} "
			kill -9 ${PID} 
			if ps -p ${PID}  > /dev/null
			then
				echo "stop failed"
			else
				echo 'stop success!'
				return 0;
			fi
			
		else
			echo "${PID}  is not running"
		fi	
	fi

	echo 根据应用名查找进程信息
	tpid=`ps -ef|grep ${APP_NAME}|grep -v grep|grep -v kill|awk '{print $2}'`
	if [ ${tpid} ]; then
		echo '$APP_NAME is running, tpid:${tpid}'
	else
		echo "没有找到$APP_NAME相关的进程"
		return 0;
	fi
	
	echo "kill -15 ${tpid}"
	kill -15 ${tpid}
	sleep 2

	tpid=`ps -ef|grep ${APP_NAME}|grep -v grep|grep -v kill|awk '{print $2}'`
	if [ ${tpid} ]; then
		echo "stop failed"
	else
		echo 'stop success!'
		return 0;
	fi
	
	echo "kill -9 ${tpid}"
	kill -9 ${tpid}

	tpid=`ps -ef|grep ${APP_NAME}|grep -v grep|grep -v kill|awk '{print $2}'`
	if [ ${tpid} ]; then
		echo "stop failed"
		return 1;
	else
		echo 'stop success!'
		return 0;
	fi	
}

echo 停止项目运行

echo 停止运行 quant-platform-gateway
stopProcess quant-platform-gateway

echo 停止运行 quant-platform-user
stopProcess quant-platform-user

echo 停止运行 quant-platform-fetch
stopProcess quant-platform-fetch

echo 停止运行 quant-platform-stock
stopProcess quant-platform-stock

echo 停止项目运行完成

echo 更新源码
cd code

echo 更新源码：java-common
cd java-common
git pull
cd ..

echo 更新源码：finance-analysis
cd finance-analysis
git pull
cd ..

echo 更新源码：finance-data
cd finance-data
git pull
cd ..

echo 更新源码：quant-platform
cd quant-platform
git pull
cd ..

cd ..
echo 更新源码完成

echo 将更新后的源码打包，将依赖项目安装到本地maven仓库
cd code

echo 打包安装到本地maven仓库：java-common
cd java-common
mvn -Dmaven.test.skip=true clean install
cd ..

echo 打包安装到本地maven仓库：finance-analysis
cd finance-analysis
mvn -Dmaven.test.skip=true clean install
cd ..

echo 打包安装到本地maven仓库：finance-data
cd finance-data
mvn -Dmaven.test.skip=true clean install
cd ..

cd ..
echo 依赖项目安装到本地maven仓库完成


echo 将项目打包
cd code

cd quant-platform
mvn -Dmaven.test.skip=true clean package
cd ..

cd ..
echo 项目打包完成

echo 项目启动

startModule()
{
	MODULE_NAME=$1;
	nohup java -Denv=prod -jar ./code/quant-platform/${MODULE_NAME}/target/${MODULE_NAME}-1.0.0.jar >./log/${MODULE_NAME}.log  2>&1 & echo $! > ./pid/${MODULE_NAME}.pid
}

echo "启动项目模块：quant-platform-gateway"
startModule quant-platform-gateway

echo "启动项目模块：quant-platform-user"
startModule quant-platform-user

echo "启动项目模块：quant-platform-fetch"
startModule quant-platform-fetch

echo "启动项目模块：quant-platform-stock"
startModule quant-platform-stock

echo 项目启动完成
```



运行过程中想要停止运行程序：

```bash
#!/bin/sh
echo =================================
echo  quant-platform 自动化部署脚本 更新项目
echo =================================

stopProcess()
{
	APP_NAME=$1;
	if [ -e  ./pid/${APP_NAME}.pid ]; then
		PID=`cat ./pid/${APP_NAME}.pid`
		echo 文件中记录的pid为 ${PID}
	else
		echo 没有找到记录PID的文件
	fi
	
	if [ ${PID} ];
	then
		echo "正在停止进程，PID：$PID"
		if ps -p ${PID} > /dev/null
		then
			echo "${PID}  is running"
			
			echo "kill -15 ${PID} "
			kill -15 ${PID} 
			sleep 2
			
			if ps -p ${PID}  > /dev/null
			then
				echo "stop failed"
			else
				echo 'stop Success!'
				return 0;
			fi

			echo "kill -9 ${PID} "
			kill -9 ${PID} 
			if ps -p ${PID}  > /dev/null
			then
				echo "stop failed"
			else
				echo 'stop success!'
				return 0;
			fi
			
		else
			echo "${PID}  is not running"
		fi	
	fi

	echo 根据应用名查找进程信息
	tpid=`ps -ef|grep ${APP_NAME}|grep -v grep|grep -v kill|awk '{print $2}'`
	if [ ${tpid} ]; then
		echo '$APP_NAME is running, tpid:${tpid}'
	else
		echo "没有找到$APP_NAME相关的进程"
		return 0;
	fi
	
	echo "kill -15 ${tpid}"
	kill -15 ${tpid}
	sleep 2

	tpid=`ps -ef|grep ${APP_NAME}|grep -v grep|grep -v kill|awk '{print $2}'`
	if [ ${tpid} ]; then
		echo "stop failed"
	else
		echo 'stop success!'
		return 0;
	fi
	
	echo "kill -9 ${tpid}"
	kill -9 ${tpid}

	tpid=`ps -ef|grep ${APP_NAME}|grep -v grep|grep -v kill|awk '{print $2}'`
	if [ ${tpid} ]; then
		echo "stop failed"
		return 1;
	else
		echo 'stop success!'
		return 0;
	fi	
}

echo 停止项目运行

echo 停止运行 quant-platform-gateway
stopProcess quant-platform-gateway

echo 停止运行 quant-platform-user
stopProcess quant-platform-user

echo 停止运行 quant-platform-fetch
stopProcess quant-platform-fetch

echo 停止运行 quant-platform-stock
stopProcess quant-platform-stock

echo 停止项目运行完成
```



停止后想要再启动：

```bash
#!/bin/sh
echo 项目启动

startModule()
{
	MODULE_NAME=$1;
	nohup java -Denv=prod -jar ./code/quant-platform/${MODULE_NAME}/target/${MODULE_NAME}-1.0.0.jar >./log/${MODULE_NAME}.log  2>&1 & echo $! > ./pid/${MODULE_NAME}.pid
}

echo "启动项目模块：quant-platform-gateway"
startModule quant-platform-gateway

echo "启动项目模块：quant-platform-user"
startModule quant-platform-user

echo "启动项目模块：quant-platform-fetch"
startModule quant-platform-fetch

echo "启动项目模块：quant-platform-stock"
startModule quant-platform-stock

echo 项目启动完成
```

