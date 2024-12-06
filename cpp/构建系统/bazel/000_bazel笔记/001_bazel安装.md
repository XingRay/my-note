# bazel安装



## 在windows上安装

在bazel页面下载 bazel 可执行文件, 添加到系统 PATH 即可



## 在ubuntu上安装

```shell
sudo apt install apt-transport-https curl gnupg
curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor >bazel-archive-keyring.gpg
sudo mv bazel-archive-keyring.gpg /usr/share/keyrings
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list
```

安装指定版本bazel

```shell
sudo apt install bazel-1.0.0
sudo ln -s /usr/bin/bazel-1.0.0 /usr/bin/bazel
bazel --version  # 1.0.0
```

安装最新版本bazel

```shell
sudo apt update && sudo apt install bazel
```

安装后升级

```shell
sudo apt update && sudo apt full-upgrade
```

