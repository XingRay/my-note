## 布局中Button始终在顶层无法被覆盖

https://blog.csdn.net/baidu_27419681/article/details/76259372

https://codeleading.com/article/45004469474/



最近写项目时遇到一个很奇怪的问题，在Relativelayout中Button始终在最顶层，无法被其他控件覆盖，然后试了下在Android 5.0以下的版本可以被正常覆盖，几经周折，终于找到了原因。

产生原因：**stateListAnimator属性**
谷歌在Material Design中推出,是一个非常简单的方法用来实现在可视状态之间平滑过渡。这个属性可以通过android:stateListAnimator进行设置，可以使控件在点击时产生不同的交互。对于Button，点击时默认有个阴影的效果用于表示按下的状态（5.0以前就是简单的变色）。
参考链接:[android5.0 StateListAnimator](http://blog.csdn.net/qq_33689414/article/details/51707397)

解决方法：

1. 如果Button已经有自定义的selector样式而不需要原生的按下后的阴影效果，可以使用android:stateListAnimator=”@null”去掉阴影效果而使Button可以被正常的覆盖
2. 如果希望保留Button的阴影效果而又想使其能够被覆盖，则应该使用个**单独的FrameLayout对Button进行包裹**，记住要给Button的下方和左右侧留出空余。这种方法虽然不符合规范，但是为了需求只得这么做了。
   代码示例：



```
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="52dp">

                <Button         
                android:id="@+id/btn"
                android:layout_width="match_parent"
                android:layout_height="42dp"
                android:layout_marginLeft="30dp"
                android:layout_marginRight="30dp"
          android:background="@drawable/login_btn_selector"
                android:text="@string/app_login"
                tools:ignore="UselessParent"
                android:textColor="@color/white"
                android:textSize="20sp" />
        </LinearLayout>
```

