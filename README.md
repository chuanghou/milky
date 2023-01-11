# milky


引入方式
```xml
<dependency>
    <groupId>com.stellariver.milky</groupId>
    <artifactId>milky-spring-boot-starter</artifactId>
    <version>latest</version>
</dependency>
```

            
提供bom文件
```xml
<dependency>
    <groupId>com.stellariver.milky</groupId>
    <artifactId>milky-dependencies</artifactId>
    <version>latest</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```
详细使用方式请看demo模块及相应测试
电脑本身有jdk，和maven的情况下，且JAVA_HOME环境变量，PATH环境变量内包含jdk及maven地址情况下
```shell
mvn clean test
```
即可

本项目还支持了maven wrapper 
所以在有JAVA_HOME的情况下
```shell
.\mvnw clean test
```
