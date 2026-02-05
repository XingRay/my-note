# PyTorch深度学习（2）——GPU版环境配置操作流程



## 1. 判断电脑中是否有 NVIDA 的 GPU

### 1.1 判断流程

打开任务管理器，点击“性能”选项卡，查看左侧一栏选项中是否包含“GPU”字样，如果有，点击对应“GPU”，然后查看右上角的 GPU 型号名称中是否包含“NVIDA”字样，如果包含，则证明电脑有 NVIDA 的 GPU。

如果左侧一栏多个选项中包含“GPU”字样，只要其中有一个 GPU 型号名称中包含“NVIDA”字样，就证明电脑装有 NVIDA 的 GPU。

![img](./assets/v2-30b6b9eaa65dcbb951b8df0f99c5e113_1440w.webp)

判断电脑中是否有 NVIDA 的 GPU

### 1.2 未找到 NVIDA 的 GPU 的情况说明

如果电脑中未安装 NVIDA 的 GPU 也可以通过 CPU 进行深度学习，但是在处理大规模深度学习任务时效率较低，导致训练时间长、能耗高。因为本文主要讲解 GPU 版深度学习环境配置，CPU 版深度学习环境配置在此不提及，若有需要请自行查找教程。

如果确定电脑上已经安装了 NIVIDA 的 GPU，但是在任务管理器中未找到，其中的一个解释是没有安装对应的驱动，导致电脑不能正常显示 NVIDA 的 GPU。

## 2. 安装 Anaconda

### 2.1 安装前准备

在配置深度学习环境的过程中，不需要预先在 Python 官网单独安装 Python，而是先安装 Anaconda，Anaconda 安装时会自动安装 Python 环境，**如果在安装 Anaconda 前已经安装了 Python，需要先卸载**。

### **2.1 下载 Anaconda 安装包**

**（1）下载最新版 Anaconda 安装包**

进入 Anaconda 最新版本下载页面：[Free Download | Anaconda](https://www.anaconda.com/download%23downloads)，可以看到不同操作系统的 Anaconda 最新版本安装包。其中的“Python 3.11”是指如果下载了这个最新版本的 Anaconda，在不同的虚拟环境中所能支持配置到的 Python 解释器的最高版本是 “3.11”。

点击 Windows 版 Anaconda 安装包“64-Bit Graphical Installer”进行下载。

![img](./assets/v2-5635b3327b4a714cd6b81e7f21ae354a_1440w.webp)

点击 Windows 版 Anaconda 安装包“64-Bit Graphical Installer”进行下载

**（2）下载旧版 Anaconda 安装包**

如果不想下载最新版 Anaconda，可以下载历史版本的 Ananconda。进入 Ananconda 历史版本下载页面：[Anaconda Installers and Packages](https://repo.anaconda.com/)，点击“Anaconda Installers”下的“View All Installers”查看 Ananconda 的所有历史版本；可以下载距今1~2年的历史版本。

本文选择“Anaconda3-5.2.0-Windows-x86_64.exe”并下载。

![img](./assets/v2-eb3a9ae4532ae82e6f98ad8f5057815e_1440w.webp)

点击“Anaconda Installers”下的“View All Installers”

![img](./assets/v2-55de593e23a682bbd93f2784eb88a4b5_1440w.webp)

选择“Anaconda3-5.2.0-Windows-x86_64.exe”并下载

### 2.3 安装流程

（1）点击 Anaconda 安装包开始安装；然后点击“Next”进行下一步；

![img](./assets/v2-3284a2e07658417294a6a07c01c69629_1440w.webp)

点击“Next”

（2）点击“I Agree”接受协议条款，然后进行下一步；

![img](./assets/v2-d5f9e93b3855734ea533104f15fd1a02_1440w.webp)

点击“ I Agree”

（3）“Just Me (recommended)”和“All Users (requires admin privileges)”随便选一个，这里选择“Just Me (recommended)”，然后点击“Next”；

![img](./assets/v2-0b9cb1d629dc68bf9d88f1f5ead4a2c7_1440w.webp)

随便选一个，然后点击“Next”

（4）选择合适的安装位置，**注意安装路径中不要出现中文**，并记住该安装路径，然后点击“Next”；

![img](./assets/v2-c27cc1ed9778bbab2d29189f30e056fe_1440w.webp)

选择合适的安装位置，然后点击“Next”

（5）“Add Anaconda to my PATH environment variable”是指将 Anaconda 添加到 my PATH 环境变量，勾选之后意味着可以在普通的命令行窗口（cmd）直接使用 Anaconda 的指令，**这里选择勾选**（如果不勾选，后续可能会出现“‘conda’不是内部或外部命令，也不是可运行的程序或批处理文件。”的问题）；“Register Anaconda as my default Python 3.6”是指将 Anaconda 注册成默认的 Python 3.6，由于 Anaconda 经常涉及到一台电脑中同时包含多个版本的情况，因此不勾选；然后点击“Install”开始安装；

![img](./assets/v2-1b9a785230fb3b36514f1186e3cf7523_1440w.webp)

勾选“Add Anaconda to my PATH environment variable”后，点击“Install”

（6）跳过安装“Microsoft Visual Studio Code”，点击“Skip”；

![img](./assets/v2-f9eec3cc00391d1d23fbeda5be218156_1440w.webp)

点击“Skip”

（7）取消全部勾选后，点击“Finish”完成安装。

![img](./assets/v2-d6ace80dcee114c6810dd3cabbdc2c2c_1440w.webp)

取消全部勾选后，点击“Finish”

### **2.4 检验 Anaconda 是否安装成功**

（1）在开始菜单打开“Anaconda Prompt”或者“Anaconda Powershell Prompt”，如果能在弹出的黑色命令行窗口看到“(base)”，则证明安装成功；

![img](./assets/v2-1283d0e42de24e4d745b821db38c48b3_1440w.webp)

黑色命令窗口出现“(base)”，证明 Anaconda 安装成功

![img](./assets/v2-00c2e582d396bb08bf0aa0a0ac89ffef_1440w.webp)

黑色命令窗口出现“(base)”，证明 Anaconda 安装成功

（2）安装不成功的情况以及相应的解决办法

①如果因为重装 Anaconda 出现了下图的报错，可参考文章（[anaconda安装问题（反复重装都不行，重装完缺少activate.bat,开始菜单少spyder等快捷方式，小弟我当初也是这样，现在都解决了）_anaconda找不到。bat-CSDN博客](https://blog.csdn.net/AI_JOKER/article/details/102844462)）进行解决，**如果还是不能解决问题，可以换装“Minconda”**。

![img](./assets/v2-c6cf4f7c80242c2cb466481d164ee6a2_1440w.png)

因为重装 Anaconda 出现“activate.bat 不是内部或外部命令，也不是可运行的程序或批处理文件。”报错

②如果在命令提示行中输入“conda list”或“conda --version”发生“‘conda’不是内部或外部命令，也不是可运行的程序或批处理文件。”的报错，可以按照下面的文章进行解决：

[VeryVast：安装Anaconda后报错“‘conda’不是内部或外部命令，也不是可运行的程序或批处理文件。”对策5 赞同 · 0 评论文章![img](./assets/v2-4ceb0526c53c5ad439b5a5031ab5a654_180x120.jpg)](https://zhuanlan.zhihu.com/p/673146585)

## 3. 管理环境

### 3.1 查看所有环境

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以查看所有环境：

```python3
conda env list
```

![img](./assets/v2-7387a836751acb0a4187b002c4999ffe_1440w.webp)

查看电脑中的所有环境

可以看到，在未创建任何虚拟环境时只存在“base”环境，此环境的位置在之前安装 Anaconda 的路径下。

### 3.2 创建虚拟环境

（1）在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```python3
conda create -n envs_name python=python_version
```

其中，envs_name 表示环境的名字；python_version 表示环境中指定的 Python 解释器的版本，**建议 Python 解释器版本高于“3.5”且低于 Anaconda 所能支持配置到的最高版本**；这里创建名为“pytorch”的虚拟环境，其中指定 Python 解释器版本为 3.10：

```python3
conda create -n pytorch python=3.10
```

![img](./assets/v2-eeb70f34aea421dcbd6e38131ab87b09_1440w.png)

在 Anaconda Prompt 命令提示行中输入“conda create -n pytorch python=3.10”

（2）以上步骤完成后，回车；在出现的“Proceed ([y]/n)?”之后输入“y”并回车，开始安装新的“packages”；

![img](./assets/v2-f105b62cedcce1ffa7451f26d1a7a771_1440w.webp)

安装新的“packages”

![img](./assets/v2-3b51769aa9d5d43f2e0f1df8f0d6a49d_1440w.webp)

新的“packages”安装完成

### 3.3 进入虚拟环境

在命令提示行中输入以下代码并回车以进入虚拟环境：

```python3
conda activate envs_name
```

其中，envs_name 表示环境的名字；比如，进入名为“pytorch”的虚拟环境：

```python3
conda activate pytorch
```

![img](./assets/v2-01e63732bccea5ef36f5106b05210cae_1440w.webp)

指定的环境名称“(pytorch)

### 3.4 查看环境的包

在新的虚拟环境的命令提示行之后输入以下代码并回车以查看新环境中的工具包：

```python3
pip list
```

或

```python3
conda list
```

“conda list”和“pip list”的区别：

- “pip list”只显示当前虚拟环境中的包；
- “conda list”除了显示虚拟环境中的包还显示关联文件下的包；
- “pip list”显示的包是“conda list”显示的包的子集。

![img](./assets/v2-a4a34d9e115b5bf685b6a52a18c822b5_1440w.webp)

新环境中的工具包

从列表中可以看到新环境中并没有我们想要的 PyTorch，因此之后还需要进行 PyTorch 的安装。

### 3.5 退出虚拟环境

如果想要退出当前虚拟环境（在删除虚拟环境之前也需要先退出当前虚拟环境），在当前虚拟环境的命令提示行中输入以下代码并回车：

```text
conda deactivate
```

比如，退出当前名为“master”的虚拟环境：

```text
conda deactivate
```

![img](./assets/v2-8e9f6e4b60c93c379961045f8a06284d_1440w.webp)

退出当前名为“master”的虚拟环境

### 3.6 删除虚拟环境

如果要删除之前创建的虚拟环境，可以在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```text
conda remove -n envs_name
```

或

```text
conda env remove -n envs_name
```

其中，envs_name 表示环境的名字。比如，删除名为“pytorch”的虚拟环境：

```text
conda remove -n pytorch
```

或

```text
conda env remove -n pytorch
```

### 3.7 补充：长格式选项和短格式选项

在命令行界面中，选项通常可以有两种形式：长格式和短格式。

- **长格式选项**（Long Options）：通常以两个连字符（--）开始，后跟完整的选项名称；例如：--name；
- **短格式选项**（Short Options）：短格式通常以一个连字符（-）开始，后跟单个字母或数字；例如：-n。

长格式和短格式是等效的，只是提供了两种不同的指定方式，以满足用户的不同偏好；比如，删除虚拟环境的命令可以写成：

```text
conda env remove -n envs_name
```

或

```text
conda env remove -name envs_name
```

## 4. 添加用于环境创建的镜像通道

**注：此部分只针对解决第 3 部分中创建虚拟环境时速度较慢的问题。**

通道（channel）相当于下载地址。当运行“conda create -n xxx”指令时，conda 就会在配置文件中通过通道来下载相应环境。在安装 Anaconda 时会默认一个通道“defaults”，它是 Anaconda 服务器的地址。Anaconda 想要下载相应的环境，就会先去配置文件中找有哪些通道，然后通过通道把相应的环境下载下来。 由于默认通道“defaults”的服务器在国外，下载过程中可能会导致速度较慢，此时就要添加国内的镜像通道来加速下载。

![img](./assets/v2-621e3f3b36f2d900fd624ec84465ad47_1440w.webp)

conda 通过通道下载相应的环境

### 4.1 添加用于环境创建的临时镜像通道

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```python3
conda create -n envs_name python=python_version -c mirror_address
```

其中，envs_name 表示环境的名字，python_version 表示环境中指定的 Python 解释器的版本，mirror_address 表示镜像通道。比如，通过清华镜像创建名为“pytorch2”、Python 解释器版本为“3.10”的虚拟环境：

```python3
conda create -n pytorch2 python=3.10 -c https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/
```

**推荐这种添加临时镜像通道来加速下载相应的环境的方法，该方法只会对当前指令生效**。修改配置文件可以达到持久添加镜像通道的目的，但是可能会造成不必要的麻烦。

### 4.2 添加用于环境创建的持久镜像通道

**（1）查看配置文件中包含的通道**

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```text
conda config --get
```

或

```text
conda config --show
```

**（2）添加用于环境创建的持久镜像通道的步骤**

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```text
conda config --add channels mirror_address
```

其中，mirror_address 表示镜像通道。比如，在配置文件中添加持久清华镜像通道：

```text
conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/
```

**（3）删除用于环境创建的持久镜像通道的步骤**

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```text
conda config --remove channels mirror_address
```

其中，mirror_address 表示镜像通道。比如，删除配置文件中添加的持久清华镜像通道：

```text
conda config --remove channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/
```

![img](./assets/v2-e71e2eaf7a04fb5d8797144328c09c37_1440w.webp)

添加、删除持久镜像通道

### 4.3 **常见的用于环境创建的镜像通道**

（1）清华镜像：

```text
https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/
```

（2）北京外国语大学镜像：

```text
https://mirrors.bfsu.edu.cn/anaconda/pkgs/main/
```

（3）阿里巴巴镜像：

```text
http://mirrors.aliyun.com/anaconda/pkgs/main/
```

## 5. 确定需要下载的 CUDA 版本

### **5.1 确定显卡算力**

**（1）显卡算力的介绍**

- **显卡**由 GPU、显存等组成，大部分情况下我们所说的 GPU 就等同于显卡，但是实际情况是 GPU 是显示卡的“心脏”、核心零部件、核心组成部分。GPU 本身并不能单独工作，只有配合上附属电路和接口才能工作，这时候，它就变成了显卡；
- **显卡算力**是指显卡处理信息的能力。

**（2）确定显卡算力的步骤**

①打开任务管理器，点击“性能”选项卡，选择 NVIDIA 的 GPU 后，在右上角查看 NVIDIA 的 GPU 型号；比如，示例电脑的 GPU 型号为“NVIDIA GeForce GTX 1660 Ti”。

![img](./assets/v2-a592e01fffd43ff37d78ac144c49e752_1440w.webp)

②根据第一步中确定的 GPU 型号查找对应的算力，不同显卡对应的算力可以查看下方链接或表格；比如示例电脑的 GPU 算力为“7.5”。

[不同显卡对应的算力zh.wikipedia.org/w/index.php?title=CUDA&oldid=78946759](https://zh.wikipedia.org/w/index.php?title=CUDA&oldid=78946759)

![img](./assets/v2-5ff611c7f3423559b2b7f3b826416435_1440w.webp)

不同显卡对应的算力

### **5.2 确定 CUDA Driver Version**

**（1）CUDA Driver Version 的介绍**

- CUDA Driver 是与 GPU 进行沟通的驱动级别底层应用程序接口，深度学习中，计算机需要安装相应的 CUDA Driver 来识别特定的显卡；CUDA Driver 是跟随 NVIDIA GPU Driver一起发布的，也就是说在安装显卡驱动时就已经安装了 CUDA Driver；
- CUDA Driver Version 的高低会影响显卡算力的释放和 CUDA Runtime Version 的选择。

**（2）确定 CUDA Driver Version 的步骤**

①在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```text
nvidia-smi
```

②输出结果的右上角可以查看“CUDA Driver Version”；比如，示例电脑的“CUDA Driver Version”为“11.6”

![img](./assets/v2-02179b5329e0c21b989dd416af107044_1440w.webp)

CUDA Driver Version 查看界面

### **5.3 确定 CUDA Runtime Version**

**（1）CUDA Runtime Version 的介绍**

- CUDA Runtime 是以 CUDA Driver 为基准开发的运行时库；
- CUDA Runtime Version 是指 CUDA 运行时的版本，也就是这一部分需要确定的 CUDA 版本。
- CUDA Driver Version 和 CUDA Runtime Version 要充分发挥显卡的算力，此外，CUDA Driver Version 还要满足 CUDA Runtime Version 的某些新功能，所以三者之间的关系需要满足：**“显卡算力对应的 CUDA 版本≤CUDA Runtime Version≤CUDA Driver Version”。**

![img](./assets/v2-2a107dd96a655116788182c76584d71e_1440w.webp)

显卡算力、CUDA Runtime Version 和 CUDA Driver Version 的关系

**（2）确定 CUDA Runtime Version 的步骤**

4.1 和 4.2 中已经确定了显卡算力和 CUDA Driver Version，根据“显卡算力对应的 CUDA 版本≤CUDA Runtime Version≤CUDA Driver Version”标准和“CUDA Runtime Version 和算力对照表”可以确定 CUDA Runtime Version。

![img](./assets/v2-ba0523f368859ad63d29a6271bb88759_1440w.webp)

CUDA Runtime Version 和算力对照表

比如，4.1 中确定的显卡算力为“7.5”，由“CUDA Runtime Version 和算力对照表”可知可选择的 CUDA 版本为“≥11.0”，4.2 中确定的 CUDA Driver Version 为“11.6”；根据“显卡算力对应的 CUDA 版本≤CUDA Runtime Version≤CUDA Driver Version”，CUDA Runtime Version 可以选择“11.0-11.6”，**下载 CUDA 时最好选择可选版本中的最高版本**，所以，这里确定需要下载的 CUDA 版本为“11.6”。

### 5.4 了解 CUDA 的安装

**（1）cuDNN**

cuDNN（CUDA Deep Neural Network library）是 NVIDIA 专门为深度学习任务而设计的加速库。cuDNN 提供了一系列高度优化的深度学习基础操作的实现，例如卷积、池化、归一化等，以便在 GPU 上高效执行神经网络的前向和反向传播。**cuDNN 通过优化深度学习的基本运算，提高了深度学习框架在 GPU 上的性能。**

**（2）CUDA 和 cuDNN 的关系**

在深度学习中，通常深度学习框架（如 PyTorch、TensorFlow 等）会与 CUDA 和 cuDNN 集成在一起，以利用 GPU 进行高效的计算。**深度学习任务可以通过 CUDA 来加速整个计算流程，并通过 cuDNN 来加速深度学习网络的运算**。这样，CUDA 和 cuDNN 共同为深度学习提供了强大的加速能力。

**（3）\*安装 PyTorch 后就不需要手动安装 CUDA 和 cuDNN**

PyTorch 的安装通常会自带适用于当前版本的 CUDA 和 cuDNN 的支持库，安装 PyTorch 时只需将 5.3 中确定好的 CUDA 版本添加在 PyTorch 的安装指令中进行安装即可。因此，安装 PyTorch 后就不需要手动安装 CUDA 和 cuDNN，手动安装 CUDA 和 cuDNN 反而会让 PyTorch 找不到它真正需要的 CUDA 和 cuDNN，从而产生报错。

①下面是安装 PyTorch 时的指令示例，可以看到指定的 CUDA 版本为“11.6”：

```text
conda install pytorch torchvision torchaudio pytorch-cuda=11.6 -c pytorch -c nvidia
```

②安装 PyTorch 时自动安装了它所需要的“cuda runtime toolkit”和“cudnn”：

![img](./assets/v2-202af63e870225df5b24c2057a307849_1440w.webp)

安装 PyTorch 时自动安装了它所需要的“cuda runtime toolkit”和“cudnn”

不过该 CUDA 只包含给 PyTorch 用的库函数，不包括 CUDA Toolchain，比如 nvcc 等等，如果需要完整的 CUDA 还是要自己安装。

## 6. 安装 PyTorch

### 6.1 使用官网命令安装最新版本的 PyTorch

如果官网的最新版 PyTorch 安装选项中可以提供 5.3 中确定的需要下载的 CUDA 版本（这里是 11.6），那么就可以通过以下流程使用官网命令安装最新版本的 PyTorch。

（1）进入 PyTorch 官网（[PyTorch](https://pytorch.org/)），点击“Get Started”后下滑进入“START LOCALLY”界面；

![img](./assets/v2-9bc28e3051e62b3967146eb22bd9ae8a_1440w.webp)

PyTorch 官网

![img](./assets/v2-9897949d60d42d2b2e386e801d412f8e_1440w.webp)

START LOCALLY 界面

（2）在“START LOCALLY”界面对想要下载的 PyTorch 版本进行配置：

- ①**“PyTorch Bulid”**表示 PyTorch 版本，“Stable”指稳定版，“Preview”指尝鲜版，一般不建议选择尝鲜版，这里选择“Stable”；
- ②**“Your OS”**表示计算机的操作系统，这里选择“Windows”操作系统；
- ③**“Package”**表示包管理器，“Conda”和“Pip”都可以使用，但优先选择“Conda”，这里选择“Conda”；
- ④**“Language”**表示 PyTorch 编程语言，这里选择 "Python"；
- ⑤**“Compute Platform”**表示 CUDA 的版本，按照 5.3 中最后确定的 CUDA 版本进行选择，这里 5.3 中确定的版本是“11.0-11.6”，没有合适的 CUDA 版本，需要下载 CUDA 的历史版本，稍后会在 6.2 部分进行说明。

（3）如果“START LOCALLY”界面中存在合适的 CUDA 版本，复制“Run this Command”后面的代码：

```text
conda install pytorch torchvision torchaudio pytorch-cuda=11.8 -c pytorch -c nvidia
```

其中，“conda install”是包的安装指令，后面跟的是具体的包的名称；由此可见，安装“PyTorch”主要是安装“pytorch”、“torchvision（提供了计算机视觉相关的功能）”和“torchaudio（提供了音频处理相关的功能）”3个包，其中“torchvision”和“torchaudio”非常小，只有几个 MB；“pytorch-cuda=11.8”表示 PyTorch 中指定的 CUDA 版本为“11.8”；“-c”是“channel”的意思，后面跟包的下载通道。

![img](./assets/v2-1243a928106b1a6475fc93535b0314be_1440w.webp)

配置 PyTorch

（4）在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以进入需要下载 PyTorch 的虚拟环境：

```text
conda activate envs_name
```

其中，envs_name 表示环境的名字；比如，这里需要为名为“pytorch”的虚拟环境安装 PyTorch，所以输入以下代码以进入“pytorch”环境：

```text
conda activate pytorch
```

![img](./assets/v2-48f95fff86959048bcc2f511ae75cbbc_1440w.webp)

进入需要安装 PyTorch 的“pytorch”虚拟环境

（5）将复制的代码粘贴到“pytorch”虚拟环境的命令提示行之后并回车，开始安装 PyTorch；

![img](./assets/v2-7121e785d82560ba339450a19c1f895c_1440w.png)

粘贴安装 PyTorch 的代码，开始安装 PyTorch

（6）在出现的“Proceed ([y]/n)?”后输入“y”并回车，开始安装新的“packages”，至此，PyTorch 的安装完成。

![img](./assets/v2-52550515e2271532539ceabdd1b980d1_1440w.webp)

开始安装新的“packages”

### 6.2 安装历史版本的 PyTorch

如果在“START LOCALLY”界面对想要下载的 PyTorch 版本进行配置时发现没有合适的 CUDA 版本或者新项目的代码只支持低版本的 PyTorch，这时就需要安装历史版本的 PyTorch；

比如，根据步骤 5 确定的需要下载的 CUDA 版本为“11.6”，然而在“START LOCALLY”界面对想要下载的 PyTorch 版本进行配置时发现只有“11.8”和“12.1”的版本，这时就需要下载历史版本。

（1）进入 PyTorch 官网（[PyTorch](https://pytorch.org/)），点击“Get Started”，然后在出现的界面中选择“Previous PyTorch Versions”；

![img](./assets/v2-57312d307e9f329808509a75a7b513bd_1440w.webp)

进入“Previous PyTorch Versions”界面

（2）进入“Previous PyTorch Versions”界面后下滑，选择“Conda”包管理器下的“Linux and Windows”系统，然后选择合适的 CUDA 版本，复制配置好的 PyTorch 版本后面的代码：

```python3
conda install pytorch==1.13.1 torchvision==0.14.1 torchaudio==0.13.1 pytorch-cuda=11.6 -c pytorch -c nvidia
```

![img](./assets/v2-e8070a13d9c55c9541b6a2283178c8e6_1440w.webp)

历史版本选择

（3）在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以进入需要下载 PyTorch 的虚拟环境：

```text
conda activate envs_name
```

其中，envs_name 表示环境的名字；比如，这里需要为名为“pytorch”的虚拟环境安装 PyTorch，所以输入以下代码以进入“pytorch”环境：

```text
conda activate pytorch
```

![img](./assets/v2-48f95fff86959048bcc2f511ae75cbbc_1440w.webp)

进入需要安装 PyTorch 的“pytorch”虚拟环境

（4）将复制的代码粘贴到“pytorch”虚拟环境的命令提示行之后并回车，开始安装 PyTorch；

![img](./assets/v2-0ac5e076b95f54a0693a1285fa45e8ca_1440w.webp)

粘贴安装 PyTorch 的代码，开始安装 PyTorch

（5）在出现的“Proceed ([y]/n)?”后输入“y”并回车，开始安装新的“packages”，至此，PyTorch 的安装完成。

![img](./assets/v2-52550515e2271532539ceabdd1b980d1_1440w.webp)

开始安装新的“packages”

### 6.3 通过镜像通道安装 PyTorch

步骤 4 中已经介绍了“通道”的概念。PyTorch 安装速度慢的主要原因是默认通道“defaults”的服务器在国外，下载过程中可能会导致速度较慢，此时就要添加国内的镜像通道来加速下载。

**（1）安装流程**

①在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车：

```python3
conda install pytorch=pytorch_version torchvision=torchvision_version torchaudio=torchaudio_version -c mirror_address
```

其中，“pytorch_version”、“torchvision_version”、“torchaudio_version”分别表示“pytorch”、“torchvision”和“torchaudio”的版本号，mirror_address 表示镜像通道。比如，在配置文件中添加持久清华镜像通道：

```python3
conda install pytorch=1.13.1 torchvision=0.14.1 torchaudio=0.13.1 -c https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/pytorch/win-64/
```

②在出现的“Proceed ([y]/n)?”后输入“y”并回车，开始安装新的“packages”，至此，PyTorch 的安装完成。

![img](./assets/v2-52550515e2271532539ceabdd1b980d1_1440w.webp)

开始安装新的“packages”

**（2）常见的用于安装 PyTorch 的镜像通道**

a.清华镜像

```python3
https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/pytorch/win-64/
```

b.北京外国语大学镜像

```python3
https://mirrors.bfsu.edu.cn/anaconda/cloud/pytorch/win-64/
```

c.阿里巴巴镜像

```python3
http://mirrors.aliyun.com/anaconda/cloud/pytorch/win-64/
```

### 6.4 本地安装 PyTorch

如果使用官网命令和通过镜像都不能成功安装 Pytorch，还可以尝试本地安装。

（1）进入 PyTorch 官网（[PyTorch](https://pytorch.org/)），点击“Get Started”后下滑进入“START LOCALLY”界面，在配置 PyTorch 版本时，选择用 Pip 包管理器，复制配置好的代码；

![img](./assets/v2-726edefa026f6deb45548a9d1a446ad8_1440w.webp)

选择用 Pip 包管理器下载 PyTorch

（2）在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以进入需要下载 PyTorch 的虚拟环境：

```python3
conda activate envs_name
```

其中，envs_name 表示环境的名字；比如，这里需要为名为“pytorch”的虚拟环境安装 PyTorch，所以输入以下代码以进入“pytorch”环境：

```python3
conda activate pytorch
```

![img](./assets/v2-48f95fff86959048bcc2f511ae75cbbc_1440w.webp)

进入需要安装 PyTorch 的“pytorch”虚拟环境

（3）将复制的代码粘贴到“pytorch”虚拟环境的命令提示行之后并回车，可以看到开始下载，但是下载速度非常慢；

![img](./assets/v2-626011b99db579a38fe9c16dd43fd2d9_1440w.webp)

下载速度非常慢

（4）由于下载速度非常慢，按下“CTRL+C”以停止下载，同时将“Downloading”后面的网址粘贴到迅雷进行下载；

（5）迅雷下载完成后，在命令提示行中输入以下代码并回车：

```python3
pip install address_xunlei
```

其中，address_xunlei 表示通过迅雷下载的 PyTorch 的本地路径；

![img](./assets/v2-ca8f97368a1af017d6589f5d24327d1e_1440w.png)

本地安装 PyTorch

（6）以上步骤完成后，继续重复步骤（3）的操作，将从官网复制的 Pip 包管理器代码粘贴到要安装 PyTorch 的虚拟环境中并回车进行下载，这次可以看到下载速度非常快，本地安装 PyTorch 完成。

## 7. 检验 PyTorch 是否安装成功

（1）PyTorch 安装完成之后，在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以进入相应的虚拟环境：

```python3
conda activate envs_name
```

其中，envs_name 表示环境的名字；比如，进入名为“pytorch”的虚拟环境：

```python3
conda activate pytorch
```

（2）进入相对应的虚拟环境之后，在命令提示行中输入以下代码并回车以查看环境中的包：

```text
conda list
```

如果能在输出的包列表中找到“torch”、“torchaudio”和“torchvision”，以及它们后面的“cu”字样，证明 PyTorch 的安装已初步成功；

![img](./assets/v2-3966d7046f28d63f021cf7655a660be5_1440w.webp)

相应虚拟环境包列表中的“torch”、“torchaudio”和“torchvision”

（3）在相应的虚拟环境的命令提示行之后输入“python”并回车进入 python 编程环境，在新出现的命令提示行中输入以下代码并回车：

```text
import torch
```

如果回车之后没有报错，则证明 PyTorch 已经安装成功；此时可以发现 Anaconda 安装路径下的文件夹“D:\Anaconda\envs”中已经出现了新环境 “pytorch”（注：该文件夹在创建虚拟环境的时候就会同时出现）；

![img](./assets/v2-2633aac627fdd9d69798cb8feb5f0ba9_1440w.webp)

PyTorch 已经安装成功

![img](./assets/v2-2ba5333838ba15e23c379ac7da7c3dc9_1440w.webp)

Anaconda 安装路径下的文件夹 envs 中出现了新环境 “pytorch”

（4）在输入“import torch”回车后的命令提示行中继续输入以下代码并回车以检验 PyTorch 是否可以使用计算机的 GPU：

```text
torch.cuda.is_available()
```

如果返回“True”，证明计算机的 GPU 可以被 PyTorch 使用。

![img](./assets/v2-2f9ec8b15516badddf45e9f92d3b9843_1440w.webp)

返回“True”

## 8. PyCharm 的安装与配置

### 8.1 安装 PyCharm

（1）PyCharm 分为“专业版”和“社区版”，“专业版”比“社区版”功能更齐全，需要付费（免费试用30天），而“社区版”是免费的，但功能可以满足大部分需求，所以这里选择下载“社区版”。进入 PyCharm 官网“[https://www.jetbrains.com/pycharm/](https://www.jetbrains.com/pycharm/)”，选择“DOWNLOAD”，继续选择“Community”版本进行下载；

![img](./assets/v2-4c65778673d96f4ef0eeff817828d235_1440w.webp)

下载 PyCharm 的 Community 版本

（2）设置**全英文安装路径**后，点击“Next”进行下一步；

![img](./assets/v2-b35e01ef7744f9bdb135b271eb1fab12_1440w.webp)

设置合适的安装路径

（3）勾选“Create Associations”，便于 PyCharm 链接所有以 .py 结尾的 python 文件，点击“Next”进行下一步；

![img](./assets/v2-c72a696372cb22b3a0d97593e47a545e_1440w.webp)

勾选“Create Associations”

（4）默认选择，点击“Install”进行安装。

![img](./assets/v2-6fd58142a03ffe54ac0b7957c1b803eb_1440w.webp)

安装 PyCharm

### 8.2 配置 PyCharm

（1）双击打开 PyCharm，点击“New Project”新建项目；

![img](./assets/v2-2067b69d795e8b4a0ee513842c9d55c6_1440w.webp)

新建项目

（2）在“Pure Python”页面分别进行以下配置：

- ①设置项目合适的保存位置；
- ②勾选“Previously configured interpreter”；
- ③点击“Add Interpreter”，选择“Add Python Interpreter”；

![img](./assets/v2-ca2ac57c05a467989ddce4b236a342ec_1440w.webp)

“Pure Python”页面中的配置

（3）选择“Add Python Interpreter”后，在“Conda Environment”页面分别进行以下配置（顺序不能乱，否则会报错）：

- ①在“Python Version”下拉选项中选择的版本要小于先前下载的 Anaconda 所能支持配置到的 Python 解释器的最高版本（这里先前下载的 Anaconda 所能支持配置到的 Python 解释器的最高版本是“3.11”，可以选择“3.11”往下的 1-2 个版本）；
- ②在“Conda executable”选项中选择**相应虚拟环境中的解释器（查找相应虚拟环境中解释器的方法详见 8.3）**（这里是“D:\Anaconda\envs\pytorch\Tools\python.exe”）；
- ③“Interpreter”选项中的选择与“②”相同；点击“OK”完成“Conda Environment”页面的配置；

![img](./assets/v2-26926acd5be2ceb57e30920f85aa9783_1440w.webp)

“Conda Environment”页面中 ① 和 ② 配置完成后的界面

![img](./assets/v2-e70af28f5abf9636a7eba97bc879e660_1440w.webp)

“Conda Environment”页面中 ③ 配置完成后的界面

（4）点击“Create”完成 PyCharm 配置；

（5）在命令提示行中输入“python”并回车进入 Python 编程环境，然后输入以下代码并回车，如果不报错，说明 PyTorch 中的包已经被成功导入了 PyCharm：

```text
import torch
```

（6）在命令提示行的 Python 编程环境中继续输入以下代码并回车，如果返回“True”，说明计算机的 GPU 可以被 PyCharm 中的 PyTorch 使用：

```text
print(torch.cuda.is_available())
```

### **8.3 补充：查找相应虚拟环境中解释器的方法**

①在“Anaconda Prompt”的“base”环境的命令提示行之后输入以下代码并回车以进入相应的虚拟环境：

```text
conda activate env_name
```

其中，“env_name”表示相应虚拟环境的名称；比如，进入名为“tiffseg”的虚拟环境：

```text
conda activate tiffseg
```

②进入相应的虚拟环境之后，在命令提示行中输入以下代码并回车以查看虚拟环境文件夹的路径：

```text
conda list
```

![img](./assets/v2-73c7a9535e899710a8d021e6b75bf082_1440w.webp)

虚拟环境文件夹的路径

可以看到，虚拟环境“tiffseg”文件夹的路径为：

```text
C:\Users\**\.conda\envs\tiffseg
```

③相应虚拟环境文件夹中的“python.exe”即为要查找的解释器。

![img](./assets/v2-ea9fbc43ff8b6ae5f6bf47017c09f394_1440w.webp)

相应虚拟环境文件夹中解释器

## 9. Jupyter 的安装与配置

### 9.1 安装 Jupyter

Jupyter 最初是 IPython 项目的一部分，它是一个用于交互式计算的 Python 解释器。Jupyter 的名称来源于三种编程语言的缩写，即 Julia、Python 和 R。通过 Jupyter，用户可以创建和共享文档，其中包含实时代码、方程、可视化图表和说明文本。Jupyter 的最大优点是能够直接在浏览器中进行交互式编程，这使得代码的编写和调试变得更加高效和便捷。

因为之前安装的 Anaconda 已经附带了 Jupyter Notebook，所以这里无需再次安装。

![img](./assets/v2-615e3131f2f5ba7f0aff5667b621df25_1440w.webp)

安装 Anaconda 时 Jupyter Notebook 被附带安装

### 9.2 配置 Jupyter

虽然 Jupyter 已经被下载，但是默认只安装在了“base”环境中；然而“base”环境中没有安装 PyTorch，所以 Jupyter 无法正常使用 PyTorch，解决办法为在“pytorch”环境中安装 Jupyter。

（1）在开始菜单打开“Anaconda Prompt”，然后在“base”环境的命令提示行后输入以下代码并回车以查看“base”环境中的所有 packages：

```text
conda list
```

输出结果中的“ipykernel”是支持 Jupyter 运行的主要的包，它是 Jupyter notebook 和 Jupyterlab 的核心部分。

![img](./assets/v2-820d8903e66891ab28248697bc67a5c3_1440w.webp)

“base”环境中的“ipykernel”包

（2）在“base”环境的命令提示行之后输入以下代码并回车进入“pytorch”环境：

```text
conda activate pytorch
```

（3）进入“pytorch”环境后，在命令提示行之后输入以下代码并回车以查看“pytorch”环境中的所有 packages：

```text
conda list
```

可以看到，输出结果中没有支持 Jupyter 运行的“ipykernel”包；

（4）在“pytorch”环境的命令提示行之后输入以下代码并回车以安装支持 Jupyter 运行的所有包：

```text
conda install nb_conda
```

（5）在出现的“Proceed ([y]/n)?”后输入“y”并回车，直至支持 Jupyter 运行的所有包安装完成；

![img](./assets/v2-d56bedbe2a7444d6ed8d720bd1756df2_1440w.webp)

支持 Jupyter 运行的所有包安装完成

（6）在“pytorch”环境的命令提示行之后输入以下代码并回车以打开 Jupyter 的操作页面：

```text
jupyter notebook
```

![img](./assets/v2-c3f5b3ce5f4d0644a60297515dc2838c_1440w.webp)

Jupyter 跳转中页面

![img](./assets/v2-0c7cdaf9d97d4478f98668775d6c0f50_1440w.webp)

Jupyter 页面

（7）点击 Jupyter 页面右侧的“New”，选择“Notebook”，接着选择“Python 3 (ipykernel)”编程语言，即可完成新 Notebook 的创建，该 Notebook 中的代码可以使用 conda 环境中的 PyTorch；创建过程中如果发生“500 : Internal Server Error”的报错，可参考文章（[jupyter notebook报错：500：Internal Server Error的解决方法_sup小鱼的博客-CSDN博客](https://blog.csdn.net/weixin_45275599/article/details/131505756)）进行解决；

![img](./assets/v2-96f54d6cb16e5279ee06bfcd281e7c0f_1440w.webp)

创建新 Notebook

![img](./assets/v2-c5cd2f597302d12300ddc36f04ebf985_1440w.webp)

选择 Python 3 (ipykernel)

（8）在命令提示行中输入以下代码并同时按下“SHIFT”和“ENTER”，如果不报错，说明 PyTorch 中的包已经被成功导入了 Jupyter：

```text
import torch
```

在命令提示行中输入以下代码并同时按下“SHIFT”和“ENTER”，如果返回“True”，说明计算机的 GPU 可以被 Jupyter 中的 PyTorch 使用：

```text
torch.cuda.is_available()
```

## 10. 为新下载的 PyTorch 项目在 PyCharm 中设置合适的虚拟环境

### 10.1 通过 GitHub 下载 PyTorch 项目

（1）登录 GitHub 官网（[GitHub](https://github.com/)），在搜索框中搜索需要下载的项目，并点击项目进入详情页；

![img](./assets/v2-c8c3ca62387e33010b913f97b4f8b68c_1440w.webp)

GitHub 官网

![img](./assets/v2-53d40eae5b4398f7ffef06cb926d4aa9_1440w.webp)

项目详情页

（2）在项目详情页点击绿色的“Code”按钮，然后在弹出的页面中点击“Download ZIP”，即可下载项目的压缩包，提前将项目压缩包解压。

![img](./assets/v2-31b785295091762d555fa21557cd83b1_1440w.webp)

在项目详情页下载项目

![img](./assets/v2-bcbf66c532fb1d22d268289c057a2ddb_1440w.webp)

项目压缩包和解压后的项目文件

### 10.2 在 PyCharm 中打开 PyTorch 项目

（1）打开 PyCharm，点击“File”，然后选择“Open”，在地址栏中选择解压好的项目所在路径并点击“OK”；

![img](./assets/v2-7b9399bb58d170fb5c6dcd2b3ff55f15_1440w.webp)

打开项目的流程

![img](./assets/v2-87481a1142f374703f9b53d489b86b91_1440w.webp)

打开解压好的项目

（2）点击“OK”后，在先后弹出的两个的对话框中分别选择“Trust Project”和“New Window”，以信任新项目并在新的窗口打开该项目。

![img](./assets/v2-9154c5cf874a2ad1256db8eeadc9a1c2_1440w.webp)

信任项目

![img](./assets/v2-f95aefe8f2defc97b04a1d417ddb3132_1440w.webp)

在新的窗口打开项目

![img](./assets/v2-33f26394a382eb403c41ba5536dadec7_1440w.webp)

成功地在 PyCharm 中打开新 PyTorch 项目

### 10.3 为 PyTorch 项目配置合适的虚拟环境

（1）点击“File”，选择“Settings”，点击 PyTorch 项目名下的“Python Interpreter”，在弹出的界面中选择“Add Local Interpreter...”；

![img](./assets/v2-8b532ab9c104f8ba0c2ebc0af124f74f_1440w.webp)

配置环境的流程

![img](./assets/v2-c601507206ba37355c3b57e48f891947_1440w.webp)

添加“Python Interpreter”

（2）选择“Add Local Interpreter...”后，在“Conda Environment”页面分别进行以下配置（顺序不能乱，否则会报错）：

- ①在“Python Version”下拉选项中选择的版本要小于先前下载的 Anaconda 所能支持配置到的 Python 解释器的最高版本（这里先前下载的 Anaconda 所能支持配置到的 Python 解释器的最高版本是“3.11”，可以选择“3.11”往下的 1-2 个版本）；
- ②在“Conda executable”选项中选择 **Anaconda 安装路径下“envs”文件夹中相应的虚拟环境名称文件夹内的“Python.exe”**（这里是“D:\Anaconda\envs\pytorch\Tools\python.exe”）；
- ③“Interpreter”选项中的选择与“②”相同；点击“OK”完成“Conda Environment”页面的配置；

![img](./assets/v2-26926acd5be2ceb57e30920f85aa9783_1440w.webp)

“Conda Environment”页面中 ① 和 ② 配置完成后的界面

![img](./assets/v2-e70af28f5abf9636a7eba97bc879e660_1440w.webp)

“Conda Environment”页面中 ③ 配置完成后的界面

（3）点击“OK”后，在返回的界面中，先点击“Apply”，再点击“OK”，即可成功为 PyTorch 项目配置合适的虚拟环境。

![img](./assets/v2-555b98fe0908d31a9e8a7b8054115c48_1440w.webp)

先点击“Apply”，再点击“OK”，完成虚拟环境的配置

## 11. 补充：查看版本信息

### 11.1 查看当前 Anaconda 的版本

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以查看当前 Anaconda 的版本：

```text
conda list anaconda
```

执行以上代码，输出结果为：

![img](./assets/v2-f40210b588bbd2704bd712e0d9868dc7_1440w.webp)

查看当前 Anaconda 的版本

由此可知，当前 Anaconda 的版本为“2023.09”.

### 11.2 查看当前 Anaconda 的版本所能支持的最高 Python 版本

（1）进入 Anaconda 的“All package lists”网站，里面记录了 Anaconda 发布的所有版本：

[All package listsdocs.anaconda.com/free/anaconda/allpkglists/](https://docs.anaconda.com/free/anaconda/allpkglists/)

（2）点击当前的 Anaconda 版本号以查看当前 Anaconda 版本所能支持的“Packages”的最高版本，这里选择“2023.09-0”；

![img](./assets/v2-898e72c3cad10aedc046cbb0be21647f_1440w.webp)

点击当前的 Anaconda 版本号

（3）在“Package Name”列检索“python”以查看不同操作系统中当前 Anaconda 的版本所能支持的最高 Python 版本，这里看到均为“3.11.5”.

![img](./assets/v2-7ee95141e9af38531dcd9b1581c63ad1_1440w.webp)

所能支持的“Packages”的最高版本查看页面

![img](./assets/v2-f7b8239867d04b0b690bce20ec23bfb5_1440w.webp)

不同操作系统中当前 Anaconda 的版本所能支持的最高 Python 版本

### 11.3 查看当前环境中的 Python 版本

“base”环境中的 Python 版本为前 Anaconda 的版本所能支持的最高 Python 版本，而创建的其他虚拟环境的 Python 版本由创建虚拟环境时（本文 3.2 部分）所设定的 Python 版本所决定；

（1）在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以查看“base”环境中的 Python 版本：

```text
python --version
```

执行以上代码，输出结果为：

![img](./assets/v2-280b2c733a4e2df15ebd7407520d667f_1440w.webp)

查看“base”环境中的 Python 版本

由此可知，“base”环境中的 Python 版本为“3.11.5”.

（2）在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以进入虚拟环境：

```text
conda activate envs_name
```

其中，envs_name 表示环境的名字；比如，进入名为“tiffseg”的虚拟环境：

```text
conda activate tiffseg
```

在命令提示行中输入以下代码并回车以查看“tiffseg”环境中的 Python 版本：

```text
python --version
```

执行以上代码，输出结果为：

![img](./assets/v2-f2e84b21af5c7e2565d6e9588362befa_1440w.webp)

查看“tiffseg”环境中的 Python 版本

由此可知，“tiffseg”环境中的 Python 版本为“3.8.18”.

### 11.4 查看当前 CUDA 的版本

打开“NVIDIA控制面板”，选择“系统信息”，在“系统信息”界面中选择“组件”，这时可以在“3D设置”一栏查看 CUDA 的版本为“12.2.79”.

![img](./assets/v2-6f5c15788a546007d227cc0905d2b5d9_1440w.webp)

打开“NVIDIA控制面板”，选择“系统信息”

![img](./assets/v2-e942b952eeeeb425bf1f58e2dee4f51a_1440w.webp)

在“系统信息”界面中选择“组件”，查看当前 CUDA 的版本

### 11.5 查看当前 conda 的版本

在开始菜单找到“Anaconda Prompt”并打开，在命令提示行中输入以下代码并回车以查看当前 conda 的版本：

```text
conda --version
```

执行以上代码，输出结果为：

![img](./assets/v2-b2c6ca8d6a56036443ce5cd1aab5aeb8_1440w.webp)

查看当前 conda 的版本

由此可知，当前 conda 的版本为“23.7.4”.