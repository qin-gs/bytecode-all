### 深入理解 JVM 字节码



#### 1. class 文件结构

u1, u2, u4 分别表示 1, 2, 4 个字节无符号整数

- 魔数 (magic number)

  u4：0xCAFEBABE，如果不符合会抛出 `java.lang.ClassFormatError`

- 版本号 (minor & major version)

  u4：副版本号 + 主版本号 (java8  52  0x34)

  如果类文件版本号高于 jvm 自身，会抛出 `java.lang.UnsupportedClassVersionError`

- 常量池 (constant pool)

  - 常量池大小 u2：如果大小为 n，最多包含 n-1 个元素，索引 1 ~ n-1，0 属于保留索引，供特殊情况使用

  - 常量池项集合

    - 类型 14 种

    - 内容

      | 类型               | tag 值 (u1) | 内容                               | 结构                                                         |
      | ------------------ | ----------- | ---------------------------------- | ------------------------------------------------------------ |
      | CONSTANT_Utf8_info | 1           | 真正的字符串内容                   | u1 tag;<br/>u2 length;<br/>u1 bytes[length];  (MUTF-8编码)   |
      | Integer            | 3           | int                                | u1 tag; <br/>u4 bytes;                                       |
      | Float              | 4           | float                              | ...                                                          |
      | Long               | 5           | long                               | u1 tag; <br />u4 high_bytes; <br/>u4_low_bytes;              |
      | Double             | 6           | double                             | ...                                                          |
      | Class              | 7           | 类 或 接口                         | u1 tag;<br />u2 name_index; (utf8)                           |
      | String             | 8           | string                             | u1 tag;<br />u2 string_index; (utf8)                         |
      | Fieldref           | 9           |                                    | u1 tag;<br />u2 class_index;<br />u2 name_and_type_index;    |
      | Methodref          | 10          | 方法                               | ...<br />方法所在类信息<br />方法名，参数，返回值类型        |
      | InterfaceMethodref | 11          |                                    | ...                                                          |
      | NameAndType        | 12          | 字段 或 方法                       | u1 tag;<br />u2 name_index; (utf8，名称)<br />u2 descriptor_index; (utf8，描述符) |
      | jdk1.7             |             |                                    |                                                              |
      | MethodHandle       | 15          |                                    |                                                              |
      | MethodType         | 16          |                                    |                                                              |
      | InvokeDynamic      | 18          | 为 invokeddynamic 指令提供引导方法 | u1 tag;<br />u2 bootstrap_method_attr_index; (引导方法表 bootstrap_methods[])<br />u2 name_and_type_index; (NameAndType 方法描述符) |

      utf-8 表示规则

      - 一个字节：0000 0001 ~ 0000 007F  (传统的 ascill 字符)
      - 两个字节：0000 0080 ~ 0000 07FF
      - 三个字节：0000 0800 ~ 0000 FFFF
      - 四个字节：0001 0000 ~ 0010 FFFF

- 类访问标记 (access flag) 

  u2：16 个标志位可以，只使用了 8 个

  - 是否为 public
  - 是否为 final
  - 不再使用
  - 是类还是接口
  - 是否为 abstract
  - 自动生成
  - 是否注解类
  - 是否枚举类

- 类索引 (this class)

- 超类索引 (super class)

- 接口表索引 (interface)

  以上三个字段来确定类的继承关系，两个字节

- 字段表 (field)

  静态 + 非静态 字段

  ```c
  // 结构
  {
      u2           fields_count; // 字段数量
      field_info   fields[fields_count]; // 字段集合
  }
  // field_info
  {
      u2               access_flags; // 字段访问标记 (public, private, protected, static, final, volatile, enum)
      u2               name_index; // 字段名
      u2               descriptor_index; // 字段描述符索引 (原始类型(B, C, D)，引用类型(String -> Ljava/lang/String;)，数组类型(int[] -> [I))
      u2               attributes_count; // 属性个数
      attribute_info   attributes[attributes_count]; // 属性集合
  }
  ```

- 方法表 (method)

  ```c
  // 结构
  {
      u2            methods_count;
      method_info   methods[methods_count];
  }
  // methods_info
  {
      u2               accss_flags; // 访问标记 (public, private, protected, static, final, synchronized, bridge, native, abstract, strictfp, 是否包含可变长度参数)
      u2               name_index; // 方法名
      u2               descriptor_index; // 方法索引 (参数 + 返回值)
      // Object foo(int i, double j, Thread t) -> (IDL/java/lang/Thread;)Ljava/lang/Object
      u2               attributes_count; // 属性个数 (抛出的异常，是否废弃)
      attribute_info   attributes[attributes_count]; // 属性集合
  }
  ```

  

- 属性表 (attribute)

  ```c
  {
      u2               attributes_count; // 属性个数
      attribute_info   attributes[attributes_count]; // 属性集合
  }
  // attribute_info 
  {
      u2   attribute_name_index; // 指向常量池的所有，得到属性名称
      u4   attribute_length; // 属性个数
      u1   info[attribute_length]; // 属性集合
  }
  ```

  静态变量初始值

  方法字节码



javap 命令，默认访问 public, protected, default 几倍的方法

-p：private 字段 方法

-s：输出类型描述符签名信息

-c：反编译出字节码

-v：更为详细的内容

-l：行号 + 局部变量表 (需要在 javac 编译的时候加上-g)





#### 2. 字节码基础



##### 字节码概述

虚拟机指令：操作码，操作数 

操作码占一个字节，最多 256 个，使用大端序(高位在前，低位在后)表示

字节码是一种介于 源码 和 机器码 中介的抽象表示

- 加载 和 存储 指令
- 控制转移指令
- 对象操作
- 方法调用
- 运算指令 和 类型转换
- 线程同步
- 异常处理



##### 虚拟机栈 和 栈帧

**整个 JVM 指令执行的过程就是局部变量表 和 操作数栈 不断加载，存储的过程**

每次方法的调用伴随者栈帧的创建，消耗；存储空间分配在 虚拟机栈 中；

StackOverflowError：线程请求分配的栈容量超过虚拟机允许的最大容量；可以用 -Xss 配置栈大小

每个栈帧都有自己的 

- 局部变量表 ：编译期可确定大小
- 操作数栈：编译器可确定
- 常量池引用：



##### 字节码指令

- 加载(load)  存储(store)  常量加载

  load：局部变量表 -> 操作数栈  (不同的数据类型 lload, fload, dload, aload)

  store：操作数栈 -> 局部变量表 (lstore, fstore, dstore, astore)

  常量加载：常量表 -> 操作数栈



- 操作数栈指令 (pop, dup, swap)

  pop：出栈 (调用有返回值的方法，但没有使用)

  dup：复制栈顶元素放入栈顶 (创建对象)

  swap：交换栈顶元素



- 运算 和 类型转换指令 (iadd, isub, idiv, imul, irem, ineg, iand, ior, ixor)

  不同数据类型运算时涉及类型转换

  - 宽化数据类型  (boolean, char, byte, short) -> int
  - 窄化数据类型(精度丢失)



- 控制转移指令
  - 条件转移 (ifeq, iflt...)
  - 符合条件转移 (tableswitch, lookupswitch)
  - 无条件转移 (goto, jsr...)



- for 语句的字节码

- switch case 实现

- String 的 switch case

  通过 hashCode 比较，如果相同调用 String#equals 方法再次比较

- i ++, ++ i

- try catch finally

  编译器采用复制 finally 代码的方式，将内容插入到 try 和 catch 代码中所有正常退出 和 异常退出之前

  返回值会被暂存，finally 中修改返回值不起作用

- try with resources

  try 和 finally 都抛出了异常，try 中的异常会被覆盖掉 (因为 finally 中的代码块会在 try 抛出异常前插入导致 finally 中的异常提前返回)

  使用 `java.lang.Throwable#addSuppressed` 避免忽略 try 中重要的异常

- 对象相关的字节码指令

  - init 方法：

    对象初始化方法，类的构造方法，非静态变量的初始化，对象初始化代码块

    成员变量的初始化 和 初始化语句 会被统一编译进 init 方法 (这就是为什么初始化成员变量会抛异常的情况下，需要将构造函数方法声明加上 throw 语句，否则编译不通过)

  - new, dup, invokespecial 创建对象

    创建一个类示例引用，复杂一个类对象示例的引用，调用 init 方法用构造函数，将对象引用存储到局部变量表
  
  - clinit 方法
  
    静态初始化方法，类静态代码块，静态变量初始化
  
    该方法的调用场景
  
    - 创建类对象：new, 反射, 反序列化
    - 访问类的静态变量 或 静态方法
    - 访问类的静态字段 或 对静态字段赋值
    - 初始化某个类的子类



#### 3. 字节码进阶

##### 方法调用指令

- invokestatic：调用静态方法

  静态绑定

- invokespecial：私有实例方法，构造函数，用 super 调用父类实例方法

  运行时动态选择具体的子类方法进行调用

- invokevirtual：非私有实例方法

  - 实例构造函数方法 init
  - private 修饰的私有实例方法 (不会被继承，编译器可以确定)
  - 使用 super 调用父类方法

- invokeinterface：接口方法

  需要运行时根据对象类型确定目标方法

  方法分派

  创建虚方法表 vtable(虚方法)，itable(支持多接口实现 offset table + method table) 

  - 多态的基础
  - 子类继承父类的 vtable，一个空类的 vtable 大小为 5 (继承自 Object)
  - 被 final + static，private 修饰的方法不会出现在 vtable 中，因为不能被重写
  - 接口方法调用使用 invokeinterface 指令，使用 itable 支持多接口的实现，itable 由 offset table 和 method table 两部分组成。调用接口方法是，现在 offset table 中查找 method table 的偏移量位置，然后在 method table 查找具体的接口实现

- invokedynamic：动态方法

  强类型语言：在编译时检查传入参数的类型 和 返回值的类型

  方法句柄 `java.lang.invoke.MethodHandler`：使得 java 可以把函数当作参数进行传递

  调用流程

  - jvm 首次执行 invokedynamic 指令时会调用引导方法 (bootstrap method)
  - 引导方法返回一个 CallSite 对象，该对象内部根据方法签名进行目标方法查找；getTarget 方法返回方法句柄对象
  - CallSite 对象没有变化的情况下，方法句柄可以一直被调用，如果有变化需要重新查找

  优点

  - 标准化：使用 Bootstrap Method, Callsite, MethodHandler 使得动态调用方法得到统一
  - 保持字节码层的统一 和 向后兼容，把动态方法的分派逻辑下放到语言实现层，未来可以进行优化修改
  - 高性能：接近原生 java 调用的性能，使用 jit 优化



##### lambda 原理

匿名内部类会生成两个 class 文件

- lambda 表达式声明的地方会生成一个 invokedynamic  指令，同时编译生成一个对应的引导方法 (Bootstrap method)
- 第一次执行 invokedynamic 指令时，会调用对应的引导方法 (Bootstrap method)，该方法会调用 `java.lang.invoke.LambdaMetafactory#metafactory` 方法动态生成内部类
- 引导方法返回 ` java.lang.invoke.CallSite` 对象，该对象的 `getTarget` 返回目标方法的句柄，该对象最终会调用实现了 Runnable 接口的内部类
- lambda 表达式中的内容会被编译成静态方法，前面动态生成的内部类会直接调用该静态方法
- 真正执行 lambda 调用的时 invokeinterface 指令

```java
public static CallSite metafactory(
    MethodHandles.Lookup caller, // caller 供 jvm 查找的上下文
    String invokedName, // 调用函数名
    MethodType invokedType,
    MethodType samMethodType, // 函数式接口定义的方法签名(参数类型 + 返回值)
    MethodHandle implMethod, // 编译时生成的 lambda 表达式对应的静态方法
    MethodType instantiatedMethodType); // 一般和 samMethodType 一样

// 该方法创建 java.lang.invoke.InnerClassLambdaMetafactory 对象，采用 asm 技术动态生成新的内部类 (lambda所在类名$$Lambda$n)
    
```

Lambda 表达式采用的方式并不是在编译期间生成匿名内部类，而是**提供一个稳定的字节码二进制表示规范**，对用户而言看到的只有 invokedynamic 这样一个非常 简单的指令。

用 invokedynamic 来实现把方法翻译的逻辑隐藏在 JDK 的实现中，后续想替换实现方式非常简单，只用修改LambdaMetafactory.metafactory 里面的逻辑就可以了。

这种方法把 Lambda 翻译的策略由编译期间**推迟到运行**时，未来的 JDK 会怎样实现 Lambda 表达式可能还会有
变化。



##### 泛型 与 字节码

泛型擦除

java 的泛型时在 javac 编译器中实现的，生成的字节码中已经不包含泛型信息

- 泛型没有自己的 class 对象
- 泛型不能用原始类型
- 不能捕获泛型异常
- 不能声明泛型数组



##### synchronized 原理

定义一个 临界区 (critical section，一次只能被一个线程执行的代码片段)

检查方法的访问标记，判断是不是同步的，如果是(ACC_SYNCHRONIZED = 1)：

- 实例方法：将当前实例对象 this 作为监视器
- 类方法：将当前类的类对象作为监视器
- 代码块：用户自定义的对象



monitorenter：执行到该指令时，会尝试获取栈顶对象对应监视器的所有权

- 成功获取：计数器置为 1
- 已经拥有：计数器 + 1
- 获取失败：阻塞当前线程至计数器变为 0

monitorexit：执行到该指令时，将计数器-1，计数器为 0 后，释放锁

编译器需要保证无论同步代码块中以何种方式结束，如果调用了 minitorenter 必须调用 monitorexit 指令；所以 编译器会自动生成一个异常处理器，一个 monitorenter 会对应 两个 monitorexit 指令

```java
public void foo() throws Throwable {
    monitorenter(lock);
    try {
        bar();
    } finally {
        monitorexit(lock);
    }
}

// 虚拟机会将 finally 中的代码复制到方法正常退出和异常退出的地方
pulic void foo() throw Throwable {
    monitorenter(lock);
    try {
        bar();
        monitorexit(lock);
    } catch(Throwable t) {
        monitorexit(lock);
    }
}
```



##### 反射的实现原理

`jdk.internal.reflect.MethodAccessor#invoke`

`jdk.internal.reflect.NativeMethodAccessorImpl#invoke`

前 15 次会调用一个 native 方法 `jdk.internal.reflect.NativeMethodAccessorImpl#invoke`，

15 次之后使用 GeneratedMethodAccessor1 调用反射方法，MethodAccessorGenerator 通过 asm 生成新的类 GeneratedMethodAccessor1





#### 4. javac 编译原理简介

词法分析 语法分析 语义分析 生成中间代码 (前端编译)

代码优化 生成目标代码 (后端编译)



##### 源码准备

[openjdk javac 源码](http://hg.openjdk.java.net/jdk8/jdk8/langtools/)



##### javac 的七个阶段

- parse：

  - 词法分析(`com.sun.tools.javac.parser.Scanner`)：将源代码拆分成 词法记号 token
  - 语法分析(`com.sun.tools.javac.parser.JavacParser`)：递归下降，生成抽象语法树

- enter：解析填充符号表 (标识符，标识符类型，作用域等)

  `com.sun.tools.javac.comp.Enter`，`com.sun.tools.javac.comp.MemberEnter`

  符号表：`com.sun.tools.javac.code.Symbol`

- process：处理注解

  `com.sun.tools.javac.processing.JavacProcessingEnvironment ` lombok

- attribute：检查语义合法性，常量折叠

  `com.sun.tools.javac.comp`包下面

  (检查变量类型，方法返回值类是否合法，是否有重复变量，类定义等)

  `com.sun.tools.javac.comp.Check#checkType` 检查方法返回值类型是否与声明的类型一致

  `com.sun.tools.javac.comp.Check#checkUnique` 检查是否有重复的定义(变量，方法)

  `com.sun.tools.javac.comp.Resolve` 检查变量，方法，类访问是否合法；为重载选择最具体的方法

  `com.sun.tools.javac.comp.ConstFold` 合并常量 (字符串象加，常量整数运算)

  `com.sun.tools.javac.comp.Infer` 推导泛型方法的参数类型

- flow：数据流分析

  `com.sun.tools.javac.comp.Flow`

  检查非 void 方法是否所有退出分支都有返回值

  检查受检异常是否被捕获 或 抛出

  检查局部变量是否被初始化

  检查 final 是否重复赋值

  检查是否有语句不可达

- desugar：去除语法糖

  泛型，内部类，try with resources，foreach，原始类型 和 包装类型转换，switch(字符串，枚举)，i++ ++i，变长参数 ...

  `com.sun.tools.javac.comp.TransTypes`：处理 泛型擦除 和 插入类型转换代码

  `com.sun.tools.javac.comp.Lower`：除泛型外的其它

  

  javac 为枚举的每个 switch 生成一个中间类，该类包含一个 SwitchMap 数组，该数组维护 枚举 ordinal 值 和 递增整数序列的映射关系

- generate：生成字节码

  遍历抽象语法树生成最终的 class 文件 `com.sun.tools.javac.jvm.Gen`

  - 初始化代码块收集到 init 和 clinit 方法

  - 将字符串拼接转换成 StringBuilder#append

  - 为 synchronized 生成异常表，保证 monitorenter 和 monitorexit 成对调用

  - 选择 switch 中使用 tabkeswitch 或 lookupswitch

    case 较稀疏时，使用 lookupswitch

    较密集时，使用 tableswitch

  

#### 5. 从字节码层面看 kotlin 语言



#### 6. asm 和 javassist 字节码操作工具



##### asm 介绍

- 基于事件触发的 core api (按顺序单向解析)
- 基于对象的 tree api (将整个结构读入内存)

三个核心类

- ClassReader赋值读取类文件字节数组，accept 调用之后该类把解析过程中的事件通知给 ClassVisitor 调用不同 visit方法

- ClassVisitor：在 visit 方法中对字节码进行修改

- ClassWriter：生成修改过的字节码



##### javassist 介绍

每个需要编辑的 class 对应一个 CtClass 实例，存储在 ClassPool 中





#### 7. Java Instrumentation 原理



##### Java Instrumentation 简介

java.lang.instrument 包实现字节码增强；可以对已加载 和 未加载的类进行修改



#####  Java Instrumentation  与 -javaagent 启动参数

使用 MANIFEST.MF 文件指定 Premain-Class 等信息，虚拟机级别的 aop



##### Jvm attach api 介绍





#### 8. JSR 269 插件化注解处理原理



##### JSR 269 简介

允许开发者在编译期间对注解进行处理，可以读取修改添加抽象语法树中的内容



##### 抽象语法树操作 api

- import com.sun.tools.javac.api.JavacTrees：语法树元素的基类
- import com.sun.tools.javac.tree.TreeMaker：创建语法树节点
- import com.sun.tools.javac.util.Names：访问标识符方法
