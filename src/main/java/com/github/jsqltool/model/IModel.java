package com.github.jsqltool.model;

import java.util.List;

import com.github.jsqltool.entity.ConnectionInfo;

public interface IModel {

	List<String> listConnection(String user);

	ConnectionInfo getConnectionInfo(String user, String connectionName);

	boolean save(String user, String oldConnectionName, ConnectionInfo info);

	boolean delete(String user, String connectionName);

}
