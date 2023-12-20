# shell脚本将本地docker镜像push到阿里云镜像仓库

```bash
#!/bin/bash

# 将本地打包的jar包生成docker镜像并push到aliyun的镜像仓库中

# api jar 所在的目录

api_files=`find /Users/yunshuodeng/Work/powerusercenter/power-dubbo-api/target -name "*.jar"`



# provider jar 所在的目录

provider_files=`find /Users/yunshuodeng/Work/powerusercenter/power-dubbo-provider/target -name "*.jar"`



# consumer jar 所在的目录

consumer_files=`find /Users/yunshuodeng/Work/powerusercenter/power-dubbo-consumer/target -name "*.jar"`



# 构建consumer docker镜像目录

consumer_des_dir="/Users/yunshuodeng/Work/mydocker/app/consumer"



# 构建provider docker镜像目录

provider_des_dir="/Users/yunshuodeng/Work/mydocker/app/provider"



# 进入构建provider docker镜像目录

cd $provider_des_dir



# 遍历api jar

for file in $api_files

do

# 判断jar文件是否存在

if [ !-f"$provider_des_dir/$file" ]; then

rm -rf "$provider_des_dir/$file"

fi



# 将api jar 复制到构建provider docker镜像目录

cp -p $file $provider_des_dir



# 判断jar文件是否存在

    if [ !-f"$consumer_des_dir/$file" ]; then

        rm -rf "$consumer_des_dir/$file"

    fi



# 将api jar 复制到构建consumer docker镜像目录

cp -p $file $consumer_des_dir



((file_count++))

done



# 复制provider jar文件

for file in $provider_files

do

# 判断jar文件是否存在

    if [ !-f"$provider_des_dir/$file" ]; then

        rm -rf "$provider_des_dir/$file"

    fi



cp -p $file $provider_des_dir

((file_count++))

done



# 复制consumer jar文件

for file in $consumer_files

do

# 判断jar文件是否存在

    if [ !-f"$consumer_des_dir/$file" ]; then

        rm -rf "$consumer_des_dir/$file"

    fi



cp -p $file $consumer_des_dir

((file_count++))

done



echo "all copy success"



# 进入构建provider docker镜像目录

cd $provider_des_dir



consumer_first=0

consumer_second=0

consumer_third=1

consumer_third_version=`docker images | grep registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_consumer | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 3`

consumer_second_version=`docker images | grep registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_consumer | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 2`

consumer_first_version=`docker images | grep registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_consumer | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 1`



echo $consumer_third_version

if [ ! -n $consumer_third_version ];then

echo "is null"

else

let consumer_third=$consumer_third_version+1

fi

echo $consumer_third

if [ ! -n $consumer_second_version ];then

echo "is null"

else

let consumer_second=$consumer_second_version

fi



if [ ! -n $consumer_first_version ];then

echo "is null"

else

let consumer_first=$consumer_first_version

fi



#echo "$versionstr"

if [ $consumer_third -gt 9 ];then

    let consumer_third=0

    let consumer_second=$consumer_second+1

    if [ $consumer_second -gt 9 ];then

        let consumer_third=0

        let consumer_second=0

        let consumer_first=$consumer_third+1

    fi

fi



consumer_next_version="$consumer_first.$consumer_second.$consumer_third"

echo $consumer_next_version



provider_first=0

provider_second=0

provider_third=1

provider_third_version=`docker images | grep registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_provider | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 3`

provider_second_version=`docker images | grep registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_provider | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 2`

provider_first_version=`docker images | grep registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_provider | sort -k2,2r | head -n 1 | awk '{print $2}'|cut -d '.' -f 1`



if [ ! -n $provider_third_version ];then

echo "is null"

else

let provider_third=$provider_third_version+1

fi



if [ ! -n $provider_second_version ];then

echo "is null"

else

let provider_second=$provider_second_version

fi



if [ ! -n $provider_first_version ];then

echo "is null"

else

let provider_first=$provider_first_version

fi



if [ $provider_third -gt 9 ];then

    let provider_third=0

    let provider_second=$provider_second+1

    if [ $provider_second -gt 9 ];then

        let provider_third=0

        let provider_second=0

        let provider_first=$provider_first+1

    fi

fi

provider_next_version=$provider_first.$provider_second.$provider_third

echo $provider_next_version



cd $provider_des_dir



# 判断镜像是否存在

# 构建provider镜像

docker build -t power-dubbo-provider:$provider_next_version .



# 进入构建consumer docker镜像目录

cd $consumer_des_dir



# 构建consumer镜像

docker build -t power-dubbo-consumer:"$consumer_next_version" .





echo "all build success"



# 上传镜像到aliyun仓库

# 登录到仓库

cat /Users/yunshuodeng/Work/mydocker/app/aliyun_images_repository_password.txt | docker login -u 阿里云账户名 --password-stdin registry.cn-shanghai.aliyuncs.com



# 将provider docker 镜像上传到user_center仓库

docker tag power-dubbo-provider:"$provider_next_version" registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_provider:"$provider_next_version" 

docker push registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_provider:"$provider_next_version"



# 将consumer docker 镜像上传到user_center_consumer仓库

docker tag power-dubbo-consumer:"$consumer_next_version" registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_consumer:"$consumer_next_version"

docker push registry.cn-shanghai.aliyuncs.com/huinongtx/user_center_consumer:"$consumer_next_version"





echo "all push success "
```

