## KubeSphere服务异常修复



1 可以将所有的非Running状态的pod全部删除



2 重新运行 ks-installer , 找到 kubersphere-system / ks-installer 重新部署

3 如果有pod一直不能正常启动, 查看pod启动失败的原因, 如 OOM killed 则修改部署配置文件中的内存限制, 适当调大一些

  