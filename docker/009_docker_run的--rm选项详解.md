docker run的--rm选项详解



在Docker容器退出时，默认容器内部的文件系统仍然被保留，以方便调试并保留用户数据。

因此，可以通过--rm命令，让容器在退出时，自动清除挂在的卷，以便清除数据：

另外，当容器退出时，通过 docker ps是看不到，需要携带-a参数：

 docker ps -a 
1
如果此时携带--rm，那么及时-a参数，也看不到该容器信息。

执行docker run命令带--rm命令选项，等价于在容器退出后，执行docker rm -v

通常--rm参数会和Foreground模式的容器使用，因为Foreground多用于测试环境，正式环境一定要保留数据。

Foreground和Detached模式区别参见 Docker容器的两种运行模式(Foreground、Detached)

