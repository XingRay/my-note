mysql数据导入



```bash
echo 开始导入数据到mysql数据库
echo 开始导入数据到主数据库
echo %currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_db.sql
%currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_db.sql

echo 开始导入数据到任务调度数据库
echo %currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_xxl_job.sql
%currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_xxl_job.sql
```

