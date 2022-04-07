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
