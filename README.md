# Jsqltools
jdbc操作数据库（增删改查）的工具，简单易用可扩展，目前基于MySQL和Oracle数据库进行开发，可以自行扩展至其他数据库。

# 相关概念

## 1.用户user
  user代表使用的用户，如果在单机模式下，user可以为空或者null，如果，连接信息使用配置文件的方式则会在classpath的dbPrfile文件夹下寻找对应的属性文件，如果user为空，则直接在根目录下寻找，如果user不为空，则从对应的名为user的目录下寻找。

# 工具配置项
   配置文件为classpath下的jsqltool.properties
  
##  1.连接信息的配置
   连接池的配置，jsqltool.model进行配置。可选值为：profiles和databaseProfile，其分别代表使用properties配置连接池，databaseProfile代表使用数据库的方式进行配置。

### 1.1 profiles 方式 
   配置项：jsqltool.model=profiles  
   profiles方式就是在指定classPath路径下方式数据库连接信息的属性文件。  
   默认情况下properties文件放在classpath下的dbProfile文件夹下，可以通过属性：jsqltool.profiles.filePath来定义方式连接信息的根路径。
   对于保存连接信息的属性文件，其文件名为连接名称，如：测试MySQL.properties，常见配置信息如下所示：
  
 ``` 
jdbc.url=jdbc\:mysql\://localhost\:3306
jdbc.className=com.mysql.jdbc.Driver
jdbc.username=root
jdbc.password=123456
  ```
  对于Oracle可以配置如下：
  
```
jdbc.url=jdbc\:oracle\:thin\:@localhost\:1521\:orcl
jdbc.className=oracle.jdbc.OracleDriver
jdbc.username=scott
jdbc.password=123456
```
&emsp;除此之外，我们也可以设置连接属性，jSqlTool框架使用的是阿里的druid作为连接池，我们可以通过这种方式来自定义连接属性，其最终会通过DruidDataSource.addConnectionProperty方法进行设置。例如对于MySQL数据库来说如果想要能够使用jdbc来返回数据库的元数据信息，则需要设置useInformationSchema属性为true，Oracle则需要设置remarksReporting属性为true。  
&emsp;注：对于MySQL和Oracle数据库分别会自动设置useInformationSchema和remarksReporting两个属性为true。  
&emsp;默认设置方式为：
  
  ```
 #设置属性信息，其默认前缀为jdbc.properties，可以通过配置文件的jsqltool.profiles.prefix属性来设置前缀信息
jdbc.properties.useInformationSchema=true
```
  
  

### 1.2 databaseProfile方式
  配置项：`jsqltool.model=databaseProfile`  
  数据库模式就是使用数据库来保存连接信息，如果第一次使用该工具则会自动根据配置信息来创建表。注：目前支持Oracle和MySQL数据库，其他数据库没有测试。  
  使用数据库的方式时还需要配置连接信息以及保存的到的表中的信息：
  
  ```
jsqltool.databaseProfile.tableName=t_jsqltool_connection_info #配置表名，该项如果不配置默认为t_jsqltool_connection_info
jsqltool.databaseProfile.className=com.mysql.jdbc.Driver
jsqltool.databaseProfile.url=jdbc\:mysql\://localhost\:3306/test?useUnicode\=true&characterEncoding\=utf-8&allowMultiQueries\=true
jsqltool.databaseProfile.username=root
jsqltool.databaseProfile.password=123456
```
  
  

### 1.3 自定义连接信息
  配置项：`jsqltool.model.customeClass=xxx.CustomerModel`  
  虽然内置了二种数据库连接信息的保存方式，但是JsqlTools并不强制你使用它们，你可以通过实现`com.github.jsqltool.model.IModel`接口来定制你的连接信息的保存方式，并且配置`jsqltool.model.customeClass`属性来指向你的实现类。示例：
  
  ```
public class IcbcModel implements IModel {

	private final ProfileModel model;
  // prop为配置信息，如果具有该构造函数，就会调用它，也可以使用无参的构造器
	public IcbcModel(Properties prop) {
		model = new ProfileModel(prop);
	}

	@Override
	public List<String> listConnection(String user) {
		return model.listConnection(user);
	}

	@Override
	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		return model.getConnectionInfo(user, connectionName);
	}

	@Override
	public boolean save(String user, String oldConnectionName, ConnectionInfo info) {
		return model.save(user, oldConnectionName, info);
	}

	@Override
	public boolean delete(String user, String connectionName) {
		return model.delete(user, connectionName);
	}

}
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

### 获取指定数据表的列信息
  方法：`List<TableColumnInfo> getTableColumnInfo(Connection connection, TableColumnsParam param)`    
    
### 获取主键信息
  方法：`Primary getPrimayInfo(Connection connection, IndexParam param)`


### 获取索引信息  
   方法：`List<Index> listIndexInfo(Connection connection, IndexParam param)`
   
 ### 删除表/视图
   支持一次性删除多张表/视图
   方法：`UpdateResult dropTable(Connection connect, DropTableParam dropTableParam)`
   参数：DropTableParam 表/视图信息的参数
   
 

  