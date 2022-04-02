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

- 字段表 (field)

- 方法表 (method)

- 属性表 (attribute)

