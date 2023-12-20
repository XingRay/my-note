# Docker隔离原理



## namespace 6项隔离 （资源隔离）  

| namespace | 系统调用参数  | 隔离内容                   |
| --------- | ------------- | -------------------------- |
| UTS       | CLONE_NEWUTS  | 主机和域名                 |
| IPC       | CLONE_NEWIPC  | 信号量、消息队列和共享内存 |
| PID       | CLONE_NEWPID  | 进程编号                   |
| Network   | CLONE_NEWNET  | 网络设备、网络栈、端口等   |
| Mount     | CLONE_NEWNS   | 挂载点(文件系统)           |
| User      | CLONE_NEWUSER | 用户和用户组               |



## cgroups资源限制 （资源限制）  

cgroup提供的主要功能如下：

资源限制：限制任务使用的资源总额，并在超过这个 配额 时发出提示

优先级分配：分配CPU时间片数量及磁盘IO带宽大小、控制任务运行的优先级

资源统计：统计系统资源使用量，如CPU使用时长、内存用量等

任务控制：对任务执行挂起、恢复等操作



cgroup资源控制系统，每种子系统独立地控制一种资源。功能如下  

| 子系统                          | 功能                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| cpu                             | 使用调度程序控制任务对CPU的使用。                            |
| cpuacct(CPU Accounting)         | 自动生成cgroup中任务对CPU资源使用情况的报告。                |
| cpuset                          | 为cgroup中的任务分配独立的CPU(多处理器系统时)和内存。        |
| devices                         | 开启或关闭cgroup中任务对设备的访问                           |
| freezer                         | 挂起或恢复cgroup中的任务                                     |
| memory                          | 设定cgroup中任务对内存使用量的限定，并生成这些任务对内存资源使用情况的报告 |
| perf_event(Linux CPU性能探测器) | 使cgroup中的任务可以进行统一的性能测试                       |
| net_cls(Docker未使用)           | 通过等级识别符标记网络数据包，从而允许Linux流量监控程序(Trawic Controller)识别从具体cgroup中生成的数据包 |



