# ElasticSearch数据导入



需要先安装 elasticdump 和 curl

```bash
npm install -g elasticdump
```



按照  analyzer/mapping/data 的顺序导入

```bash
echo 开始导入数据到 ElasticSearch

echo 导入数据到索引 %esIndexPrefix%_pt
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=analyzer
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=analyzer
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_mapping.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=mapping
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_mapping.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=mapping
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_data.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=data
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_data.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=data

echo 导入数据到索引 %esIndexPrefix%_goods
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=analyzer
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=analyzer
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_mapping.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=mapping
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_mapping.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=mapping
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_data.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=data
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_data.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=data

echo 开启索引
cmd /c curl -X POST http://%esLocation%/%esIndexPrefix%_pt/_open
cmd /c curl -X POST http://%esLocation%/%esIndexPrefix%_goods/_open
```

导入完成后索引是关闭的，需要通过POST的方式访问 `/<index-name>/_open`开启索引