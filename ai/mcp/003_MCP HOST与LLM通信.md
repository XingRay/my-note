# MCP HOST 与 LLM 通信

## 1 下载源码

https://github.com/MarkTechStation/VideoCode

```shell
VideoCode/MCP终极指南-番外篇
	llm_logger.py
	requirements.txt
```



## 2 运行proxy服务器

### 1 创建虚拟环境

```
python -m venv .venv
```



### 2 激活虚拟环境

```shell
.venv\Scripts\activate
```



### 3 安装依赖

```shell
pip install -r requirements.txt
```



### 4 启动服务器

```shell
python llm_logger.py
```

注意：原作者使用 openrouter， 这里使用 DeepSeek 需要修改源码中的请求 url ， 43行修改为：

```
"https://api.deepseek.com/chat/completions",
```



## 3 设置使用本地服务器

启动vscode，启动Cline，

修改配置：
API Provider： OpenAI Compatible

BaseUrl： http://localhost:8000

OpenAI Compatible API Key:  --- DeepSeek API KEY -----

Model ID: deepseek-chat

注： 
**deepseek-chat**  **DeepSeek-V3.2****（非思考模式）**
**deepseek-reasoner** **DeepSeek-V3.2****（思考模式）**

https://api-docs.deepseek.com/zh-cn/quick_start/pricing



