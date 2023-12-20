## git 常用指令



在本地创建一个全新的项目，完成初始代码，提交到本地仓库master分支后，在github/gitee平台创建一个远程仓库



### create a new repository on the command line



```
echo "# project-name" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M master
git remote add origin git@github.com:UserName/project-name.git
git push -u origin master
```



### push an existing repository from the command line



```
git remote add origin git@github.com:UserName/project-name.git
git branch -M master
git push -u origin master
```
