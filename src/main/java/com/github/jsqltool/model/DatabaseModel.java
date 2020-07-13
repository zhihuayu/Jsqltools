package com.github.jsqltool.model;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.profile.DataBaseProfile;

public class DatabaseModel implements IModel {

	private final DataBaseProfile profile;

	public DatabaseModel(Properties prop) {
		this.profile = new DataBaseProfile(prop);
	}

	public DatabaseModel(String driverClassName, String url, String userName, String password) {
		this.profile = new DataBaseProfile(driverClassName, url, userName, password);
	}

	@Override
	public List<String> listConnection(String user) {
		try {
			return profile.listConnection(user);
		} catch (SQLException e) {
			throw new JsqltoolParamException(e);
		}
	}

	@Override
	public boolean save(String user, String oldConnectionName, ConnectionInfo info) {
		try {
			return profile.save(user, oldConnectionName, info);
		} catch (SQLException e) {
			throw new JsqltoolParamException(e);
		}
	}

	@Override
	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		return profile.getConnectionInfo(user, connectionName);
	}

	@Override
	public boolean delete(String user, String connectionName) {
		return profile.delete(user, connectionName);
	}

	@Override
	public List<ConnectionInfo> listConnectionInfo(String user) {
		try {
			return profile.listConnectionInfo(user);
		} catch (SQLException e) {
			throw new JsqltoolParamException(e);
		}
	}

}
