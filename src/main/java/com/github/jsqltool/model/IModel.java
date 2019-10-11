package com.github.jsqltool.model;

import java.util.List;

import com.github.jsqltool.entity.ConnectionInfo;

public interface IModel {

	/**
	 * 
	* @author yzh
	* @date 2019年10月10日
	* @Description: 获取指定用户的所有连接的名称
	 */
	List<String> listConnection(String user);

	/**
	* @author yzh
	* @date 2019年10月10日
	* @Description: 获取指定用户和连接名称的连接信息
	 */
	ConnectionInfo getConnectionInfo(String user, String connectionName);

	/**
	 * 
	* @author yzh
	* @date 2019年10月10日
	* @Description: 保存连接信息（实现子类使用该方法需要具备添加和修改的功能）
	 */
	boolean save(String user, String oldConnectionName, ConnectionInfo info);

	/**
	 * 
	* @author yzh
	* @date 2019年10月10日
	* @Description: 删除指定的连接信息
	 */
	boolean delete(String user, String connectionName);

}
