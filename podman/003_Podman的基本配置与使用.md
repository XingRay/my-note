Podman的基本配置与使用


一、Podman的基本设置和使用
run运行容器
这个示例容器将运行一个非常基本的 httpd 服务器，它只服务于它的索引页面。

[root@localhost ~]# podman run -dt -p 8080:8080/tcp -e HTTPD_VAR_RUN=/run/httpd -e HTTPD_MAIN_CONF_D_PATH=/etc/httpd/conf.d -e HTTPD_MAIN_CONF_PATH=/etc/httpd/conf -e HTTPD_CONTAINER_SCRIPTS_PATH=/usr/share/container-scripts/httpd/ registry.fedoraproject.org/f29/httpd /usr/bin/run-httpd
1
ps列出正在运行的容器
如果在ps命令中添加-a的参数，Podman 将显示所有容器。

[root@localhost ~]# podman ps
CONTAINER ID  IMAGE                                        COMMAND               CREATED             STATUS                 PORTS                   NAMES
5106f596ee37  registry.fedoraproject.org/f29/httpd:latest  /usr/bin/run-http...  About a minute ago  Up About a minute ago  0.0.0.0:8080->8080/tcp  elastic_fermi
1
2
3
inspect检查正在运行的容器
//这是用grep命令过滤出想看的信息，单独的使用inspect会列出全部信息。-l参数是latest，显示最新的容器。
[root@localhost ~]# podman inspect -l | grep IPAddress\"
            "IPAddress": "10.88.0.3",
                    "IPAddress": "10.88.0.3",
1
2
3
4
注意： -l 是latest的缩写，最新容器的方便参数。您也可以使用容器的 ID 代替 -l。

测试 httpd 服务器
[root@localhost ~]# curl 10.88.0.3:8080
或
[root@localhost ~]# curl 127.0.0.1:8080
1
2
3
查看容器的日志
[root@localhost ~]# podman logs -l
=> sourcing 10-set-mpm.sh ...
=> sourcing 20-copy-config.sh ...
=> sourcing 40-ssl-certs.sh ...
AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 10.88.0.3. Set the 'ServerName' directive globally to suppress this message
[Tue Aug 16 02:12:24.498326 2022] [ssl:warn] [pid 1:tid 140276001877376] AH01882: Init: this version of mod_ssl was compiled against a newer library (OpenSSL 1.1.1b FIPS  26 Feb 2019, version currently loaded is OpenSSL 1.1.1 FIPS  11 Sep 2018) - may result in undefined or erroneous behavior
[Tue Aug 16 02:12:24.499208 2022] [ssl:warn] [pid 1:tid 140276001877376] AH01909: 10.88.0.3:8443:0 server certificate does NOT include an ID which matches the server name
........................
1
2
3
4
5
6
7
8
查看容器的 pid
[root@localhost ~]# podman top -l
USER        PID         PPID        %CPU        ELAPSED           TTY         TIME        COMMAND
default     1           0           0.000       14m15.909368096s  pts/0       0s          httpd -D FOREGROUND
default     23          1           0.000       14m15.909535089s  pts/0       0s          /usr/bin/coreutils --coreutils-prog-shebang=cat /usr/bin/cat
default     24          1           0.000       14m15.909567039s  pts/0       0s          /usr/bin/coreutils --coreutils-prog-shebang=cat /usr/bin/cat
...................
1
2
3
4
5
6
检查点容器
检查点容器会停止容器，同时将容器中所有进程的状态写入磁盘。有了这个，容器可以稍后恢复并在与检查点完全相同的时间点继续运行。此功能不支持无根，需要以 root 身份创建的容器

[root@localhost ~]# podman container checkpoint 5106f596ee37
1
恢复容器
仅对以前设置检查点的容器才能恢复容器。恢复的容器将继续在与检查点完全相同的时间点运行。要恢复容器，请使用：

[root@localhost ~]# podman container restore 5106f596ee37
5106f596ee371a97704f4762a8bbbd8af29764f639521be95e4968397cc4796b
1
2
迁移容器
要将容器从一台主机实时迁移到另一台主机，容器会在迁移的源系统上设置检查点，转移到目标系统，然后在目标系统上恢复。传输检查点时，可以指定输出文件。

在源系统上：

[root@localhost ~]# podman container checkpoint 5106f596ee37 -e /tmp/checkpoint.tar.gz
5106f596ee371a97704f4762a8bbbd8af29764f639521be95e4968397cc4796b
[root@localhost ~]# scp /tmp/checkpoint.tar.gz 192.168.92.130:/tmp
The authenticity of host '192.168.92.130 (192.168.92.130)' can't be established.
ECDSA key fingerprint is SHA256:41MUAgoOJ7cipkGboXt2n0BlrxuPxp2IVlgXn0ahNgg.
Are you sure you want to continue connecting (yes/no/[fingerprint])? yes
Warning: Permanently added '192.168.92.130' (ECDSA) to the list of known hosts.
root@192.168.92.130's password:
checkpoint.tar.gz                                                                        100% 1022KB 100.5MB/s   00:00
1
2
3
4
5
6
7
8
9
在目标系统上：

[root@node01 ~]# ll /tmp/checkpoint.tar.gz
-rw------- 1 root root 1046471 Aug 16 10:38 /tmp/checkpoint.tar.gz
[root@node01 ~]# podman container restore -i /tmp/checkpoint.tar.gz
Trying to pull registry.fedoraproject.org/f29/httpd:latest...
Getting image source signatures
Copying blob 7692efc5f81c skipped: already exists  
Copying blob aaf5ad2e1aa3 done  
Copying blob d77ff9f653ce done  
Copying config 25c76f9dcd done  
Writing manifest to image destination
Storing signatures
5106f596ee371a97704f4762a8bbbd8af29764f639521be95e4968397cc4796b
1
2
3
4
5
6
7
8
9
10
11
12
停止容器
[root@localhost ~]# podman stop 5106f596ee37
5106f596ee37
[root@localhost ~]# podman container ls -a
CONTAINER ID  IMAGE                                        COMMAND               CREATED         STATUS                    PORTS                   NAMES
5106f596ee37  registry.fedoraproject.org/f29/httpd:latest  /usr/bin/run-http...  33 minutes ago  Exited (0) 7 seconds ago  0.0.0.0:8080->8080/tcp  elastic_fermi
1
2
3
4
5
删除容器
[root@localhost ~]# podman rm --latest
5106f596ee371a97704f4762a8bbbd8af29764f639521be95e4968397cc4796b
[root@localhost ~]# podman container ls -a
CONTAINER ID  IMAGE       COMMAND     CREATED     STATUS      PORTS       NAMES

1
2
3
4
5
设置容器开机自启
语法：podman generate systemd [options] {CONTAINER|POD}
选项：
--container-prefix string	//容器的Systemd单元名称前缀（默认为“容器”）
--files, -f			//生成.service文件
--format string 		//以指定格式（json）打印创建的单位
--name, -n			//使用容器/容器名称，而不是ID
--new				//创建新容器，而不是启动现有容器
--no-header			//跳过标题生成
--pod-prefix string 		//pod的Systemd单元名称前缀（默认为“pod”）
--restart-policy string		//Systemd重新启动策略（默认为“故障时”）
--separator string		//Systemd单元名称：名称/id和前缀之间的分隔符（默认“-”）
--time uint, -t			//停止超时覆盖（默认值为10）
1
2
3
4
5
6
7
8
9
10
11
12
操作演示：

//运行一台容器
[root@localhost ~]# podman container run -d --name web -p 80:80 -v /opt/data:/data httpd

//进入到/etc/systemd/system或/usr/lib/systemd/system，因为生成的service文件默认在当前目录
[root@localhost system]# cd /etc/systemd/system
//--files生成.service文件，--new创建新容器
[root@localhost system]# podman generate systemd --name web --files --new

//删除已有的web容器
[root@localhost system]# podman rm -f web
455266d2f50f4d0f47a6c84b08c0357b12ae4a4cdfd4e1a2e0023abb277a3764

//设置开机自启
[root@localhost system]# systemctl daemon-reload
[root@localhost system]# systemctl enable --now container-web.service
Created symlink /etc/systemd/system/multi-user.target.wants/container-web.service → /etc/systemd/system/container-web.service.
Created symlink /etc/systemd/system/default.target.wants/container-web.service → /etc/systemd/system/container-web.service.
//可以看到自动生成的新容器
[root@localhost system]# podman container ls
CONTAINER ID  IMAGE                           COMMAND           CREATED             STATUS                 PORTS               NAMES
0fe8071993d1  docker.io/library/httpd:latest  httpd-foreground  About a minute ago  Up About a minute ago  0.0.0.0:80->80/tcp  web

//重启宿主机验证该容器是否能开机自启

1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
无根root模式设置容器和上面这种方式大同小异，使用systemctl命令带上 --user 即可

#需要运行loginctl enable-linger命令，使用户服务在服务器启动时自动启动即可
[containers@localhost ~]$ loginctl enable-linger 
1
2
查看生成的.service文件

[root@localhost system]# cat container-web.service
# container-web.service
# autogenerated by Podman 3.3.1					
# Mon Aug 15 18:23:29 CST 2022					#该文件创建的时间戳

[Unit]		#单元
Description=Podman container-web.service		#描述信息
Documentation=man:podman-generate-systemd(1)	#man帮助以及生成的系统
Wants=network-online.target						#网络
After=network-online.target
RequiresMountsFor=%t/containers

[Service]
Environment=PODMAN_SYSTEMD_UNIT=%n
Restart=on-failure						#故障时重启
TimeoutStopSec=70						#超时的时间
ExecStartPre=/bin/rm -f %t/%n.ctr-id
ExecStart=/usr/bin/podman container run --cidfile=%t/%n.ctr-id --sdnotify=conmon --cgroups=no-conmon --rm --replace -d --name web -p 80:80 -v /opt/data:/data httpd
ExecStop=/usr/bin/podman stop --ignore --cidfile=%t/%n.ctr-id
ExecStopPost=/usr/bin/podman rm -f --ignore --cidfile=%t/%n.ctr-id
Type=notify
NotifyAccess=all

[Install]
WantedBy=multi-user.target default.target
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25