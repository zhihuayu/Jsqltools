package com.github.jsqltool.model;

import java.util.List;

import com.github.jsqltool.entity.ConnectionInfo;

public class DatabaseModel implements IModel {

	@Override
	public List<String> listConnection(String user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean save(String user, ConnectionInfo info) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean delete(String user, String connectionName) {
		// TODO Auto-generated method stub
		return false;
	}

}
