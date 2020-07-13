package com.github.jsqltool.model;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.exception.CannotFindProfileException;
import com.github.jsqltool.profile.FileProfile;

public class ProfileModel implements IModel {

	private final FileProfile fileProfile;

	public ProfileModel(Properties prop) {
		fileProfile = new FileProfile(prop);
	}

	@Override
	public List<String> listConnection(String user) {
		return fileProfile.listProfilesName(user);
	}

	@Override
	public boolean save(String user, String oldConnectionName, ConnectionInfo info) {
		try {
			fileProfile.saveConnectionInfo(user, oldConnectionName, info);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public ConnectionInfo getConnectionInfo(String user, String connectionName) {
		try {
			return fileProfile.loadConnectionInfo(user, connectionName);
		} catch (Exception e) {
			throw new CannotFindProfileException("找不到对应的连接信息", e);
		}
	}

	@Override
	public boolean delete(String user, String connectionName) {
		return fileProfile.delete(user, connectionName);
	}

	@Override
	public List<ConnectionInfo> listConnectionInfo(String user) {
		return fileProfile.listConnectionInfo(user);
	}

}
