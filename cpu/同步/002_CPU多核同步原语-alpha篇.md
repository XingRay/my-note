# CPU多核同步原语-alpha篇

在[Xinzhao：CPU多核同步原语](https://zhuanlan.zhihu.com/p/48460953)里，我们在引入rmb(read memory barrier)时候，曾经介绍一种弱化形式的rmb，即带data dependency的rmb。那个时候，我们提到过，对于alpha体系，应当特殊看待。现在，我们就来认真思考一下，alpha的体系为何特殊。

本部分的内容主要参考

[![img](https://pic3.zhimg.com/v2-2d47e939feed796bcf7483d306661c88_ipico.jpg)https://stackoverflow.com/questions/35115634/dependent-loads-reordering-in-cpustackoverflow.com/questions/35115634/dependent-loads-reordering-in-cpu](https://link.zhihu.com/?target=https%3A//stackoverflow.com/questions/35115634/dependent-loads-reordering-in-cpu)

有兴趣的同学请直接参考这个回答。

首先，我们来看一个简单的例子，假设初始化的时候，`int x = 1, y = 0; int*p = &x;`接下来 ，一个线程`t0`执行：

```cpp
y = 1;
wmb();
p = &y;
```

另一个线程`t1`执行：

```text
int k = *p;
```

那么对于线程`t1`而言，所load到的值`k`可能是多少呢？结果很让人惊讶：在alpha体系中，`k`的值可能是0！这是怎么发生的呢？我们继续往下看。

从传统上分析，`t0`中，`wmb`将程序分成两部分，无论`t1`发生在`wmb`之前（`*p`获得的是x的值），还是wmb之后（`*p`获得了最新的[y值](https://zhida.zhihu.com/search?content_id=173988145&content_type=Article&match_order=1&q=y值&zhida_source=entity)），`k`的值都不可能是0.然而，在alpha体系了，偏偏可能发生这么一种情况：尽管有`wmb`的保证，并且`t1`发生在`wmb`之后，然而对于`t1`而言，**好像**对于指针`p`的修改（即`p = &y`部分）发生在了`y=1;`之前，导致`k`获得了[p值](https://zhida.zhihu.com/search?content_id=173988145&content_type=Article&match_order=1&q=p值&zhida_source=entity)修改后，指向的y的旧值。but, how?

现在，我们还是拿出我们之前的那个典型的smp设计图来讲解一下：

```text
+-------------+                  +-------------+   
      |             |                  |             |   
      |   core 0    |                  |   core 1    |   
      |             |                  |             |   
      +-^-------v---+                  +-^-------v---+   
        ^       v                        ^       v       
        ^       v                        ^       v       
        ^     +-------+                  ^     +-------+ 
        ^<<<<<| store |                  ^<<<<<| store | 
        ^>>>>>|  buf  |                  ^>>>>>|  buf  | 
        ^     +-v-----+                  ^     +-v-----+ 
        ^       v                        ^       v       
        ^       v                        ^       v       
      +-^-------v---+                  +-^-------v---+   
      |             |                  |             |   
      |    cache    |                  |    cache    |   
      |             |                  |             |   
      +-------------+                  +-------------+   
             |                                |          
    +------------------+             +------------------+
    | invalidate queue |             | invalidate queue |
    +------------------+             +------------------+
             |                                |          
             |                                |          
             __________________________________
                            |
                            |
          +--------------------------------------+
          |                                      |
          |             Main Memory              |
          |                                      |
          +--------------------------------------+
```

假设`t0`运行在`core 0`上，`t1`运行在`core 1`上。开始的时候，`t0`修改`y`的值，因为这个值可能被`core 1 `cache住，所以`core 0`发送`invalidate y`的请求给`core`1, 并且迅速获得了`core 1`回复的ack。此时，这个`invalidate y`的操作pending在了`core 1`的invalidate queue中。

`t0`执行`wmb`，将自己对y的修改 flush到全局内存序中。然后，执行对指针`p`对修改。由于某种原因，`p`现在在`core 0`中处于`exclusive`状态，因此`core 0`可以安心地修改`p`的值。

接下来, `t1`开始执行。`t1`首先获取`p`的值。由于之前`p`位于`t0`的cache中，且处于`exclusive`状态，因此`t1`可以无争议地获得`p`的最新值（`t0`和`t1`关于对`p`的修改，并没有严格同步，这是程序的设计问题。我们在这里仅仅讨论一种可能性）。 接下来，`t1`来load `*p`，由于*invalidate y的操作还pending在invalidate queue里，因此，`core 1`是有可能load到一个旧值的，即，`k`值为0！

问题出在哪里呢？问题就出现在，对于y的`invalidate`请求还没有完成，`core 1`就需要去load y的值了。

首先，我们考量一下，这么做会不会违背我们的program order，即程序顺序。对于[invalidate queue](https://zhida.zhihu.com/search?content_id=173988145&content_type=Article&match_order=5&q=invalidate+queue&zhida_source=entity)，我们讲过，为了避免违背program order，对于变量的load，需要考虑其是否可能存在于`invalidate queue`中。如果`invalidate queue`有对于变量的修改，`core 1`需要首先完成invalidate动作，才能继续进行接下来的操作。

对于例子里的`core 1`而言，其需要load的是`*p`，`*p`可能指向`x`，也可能指向`y`，因此并不会于`invalidate queue`里pending的`invalidate y`操作匹配，所以`core 1`认为`invalidate queue`不需要flush，可以继续进行之后的操作，才会从自己的cache中load到了y的旧值。

如果`t1`使用的是`int k = y`，而不是`int k = *p`，那么上述所描述的对于`invalidate queue`的检查就会发现有一个pending的对于y的invalidate需要完成。如果`t1`发生在`t0`的`wmb`之后，`k`的值就只能是`1`。（当然，load y之前需要一个rmb来trigger这种对于invalidate queue的检查）

如何解决这种问题呢，除了修改硬件设计，加入相关的depdency检测外，软件层面上有两个思路：

1. 在`t1`去load `*p`之前，来一个strong的rmb，要求pending在`core 1`上的所有invalidate queue都做完，这就是我们之前介绍的rmb了
2. 在`t1`去load *p之前，来一个weak的rmb，要求pending在`core 1`上的invalidate queue中可能带有dependency的`invalidate`都做完，这就是我们之前介绍的带data depedency的[memory barrier](https://zhida.zhihu.com/search?content_id=173988145&content_type=Article&match_order=2&q=memory+barrier&zhida_source=entity)了

我们现在来看一段来自于

[http://www.puppetmastertrading.com/images/hwViewForSwHackers.pdfwww.puppetmastertrading.com/images/hwViewForSwHackers.pdf](https://link.zhihu.com/?target=http%3A//www.puppetmastertrading.com/images/hwViewForSwHackers.pdf)

的代码：

```text
1 struct el *insert(long key, long data)
2 {
3     struct el *p;
4     p = kmalloc(sizeof(*p), GPF_ATOMIC);
5     spin_lock(&mutex);
6     p->next = head.next;
7     p->key = key;
8     p->data = data; 
9     smp_wmb();
10    head.next = p;
11    spin_unlock(&mutex);
12 }
13
14 struct el *search(long key)
15 {
16     struct el *p;
17     p = head.next;
18     while (p != &head) {
19         /* BUG ON ALPHA!!! */
20         if (p->key == key) {
21             return (p);
22         }
23         p = p->next;
24     };
25     return (NULL);
26 }
```

19行的注释说明，对于alpha平台，这可能导致bug。现在我们来分析一下可能会造成什么bug。这段代码的逻辑和我们之前举的非常类似。第9行的wmb保证了之前的store对于全局内存序可见。之后才将`head.next`赋值为`p`指针。在17行，另一个线程可能load到了最新的`head.next`，即最新的`p`指针的值。由于在第20行解引用`p`的时候，没有做read memory barrier （无论是strong还是weak的），这里是有可能出现：虽然`p`的[指针值](https://zhida.zhihu.com/search?content_id=173988145&content_type=Article&match_order=1&q=指针值&zhida_source=entity)是新的（load自`head.next`），但是其指向的内存却是out-of-date的。例如，第7行对`p->key`的修改，有可能还pending在`invalidate queue`里没有完成，就执行了第20行的load操作，返回一个错误的`p->key`值。

目前，这种由于缺乏正确处理core的invalidate queue，而导致的多核同步失败的场景，只可能出现在alpha平台上。并且，alpha的这一个设计证明，简洁设计带来的性能提升，不足以抵消起带来的软件层面复杂度的提升，以及在必要的时候做memory barrier所带来的性能损耗。所以，在alpha之后，就再也没有smp采用这种设计了。



关于alpha体系，以及c++11种引入的release-consume语义，我目前感觉仍然是一头雾水。根据concurrency with modern c++的说法，在当前的时间点，所有的[编译器](https://zhida.zhihu.com/search?content_id=173988145&content_type=Article&match_order=1&q=编译器&zhida_source=entity)都将release-consume提升到release-acquire对待，因此，即使是在代码里使用release-consume，相比于更加严格的release-acquire所带来的性能提升仅仅存在于理论上，在实践中两者是一回事（因为编译器这么干了）。从这一方面来讲，c++11的memory model definition仍然是不尽完善的 。