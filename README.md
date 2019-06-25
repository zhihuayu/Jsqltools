# Jsqltools
<pre>  
  jdbc操作数据库（增删改查）的工具，简单易用可扩展，目前基于MySQL和Oracle数据库进行开发，可以自行扩展至其他数据库。
</pre>

# 相关概念

## 1.用户user
<pre>
  user代表使用的用户，如果在单机模式下，user可以为空或者null，如果，连接信息使用配置文件的方式则会在classpath的dbPrfile文件夹下寻找对应的属性文件，如果user为空，则直接在根目录下寻找，如果user不为空，则从对应的名为user的目录下寻找。
  注：连接信息可以使用数据库的方式进行配置【待实现】。
</pre>  



# 连接池信息的配置方式
<pre>
   连接池的配置，可以通过com.github.jsqltool.config下的config.properties文件的jsqltool.model进行配置。可选值为：profiles和databaseProfile，其分别代表使用properties配置连接池，databaseProfile代表使用数据库的方式进行配置。
</pre>
## profiles方式
  properties文件放在classpath下的dbProfile下。
  其中，文件名为连接名称，如：测试MySQL.properties，常见配置信息如下所示：
  
 ``` jdbc.url=jdbc\:oracle\:thin\:@localhost\:1521\:orcl
  jdbc.className=oracle.jdbc.OracleDriver
  jdbc.username=scott
  jdbc.password=123456 
  ```




# 使用方式
  该工具的入口类为：`com.github.jsqltool.config.JsqltoolBuilder`,该类是一个单例的门面类【懒加载模式】，通过该类我们可以配置工具的相关信息比如添加类型解析器，分页的dialect等等，也可以直接通过该类来获取我们想要的信息。
### 实例化JsqltoolBuilder  
  使用JsqltoolBuilder.builder()方法，就可以实例化该对象，实例化之后，我们就可以通过该类进行各类操作了。
  
### 获取连接信息
  方法：`List<String> listAllConnectionName(String user)`
  该方法用以获取配置的连接信息，如果是单例模式user可以为空或者null值

### 获取`java.sql.Connection`实例
  方法：`Connection connect(String user, String connectionName)`
  参数说明：user为用户，单例模式下为空或者null，connectionName为连接信息【上一步获取到的连接信息】

### 获取catelog信息
  方法：`List<String> listCatelog(Connection connection)`
  参数：connection为`java.sql.Connection`实例
  说明：catelog信息一般数据为空，对于`MySQL`来说就是数据库的名称

### 获取schema信息
  方法：`List<String> listSchema(Connection connection, String catelog)`
  参数：connection为`java.sql.Connection`实例，catelong为上一步获取的信息，如果是MySQL数据库，则为数据库信息，如果没有则可以空着。
  说明：schema信息一般数据为空，对于`Oracle`来说就是用户的名称

### 获取指定数据库中的table信息
  方法：`List<SimpleTableInfo> listTable(Connection connection, TablesParam param)`
    
  
  