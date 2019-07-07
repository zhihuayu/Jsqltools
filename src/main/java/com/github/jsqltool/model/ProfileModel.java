package com.github.jsqltool.model;

import java.io.IOException;
import java.util.List;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.exception.CannotFindProfileException;
import com.github.jsqltool.profile.FileProfile;

public class ProfileModel implements IModel {

	private final FileProfile fileProfile;

	public ProfileModel(String path) {
		fileProfile = new FileProfile(path);
	}

	@Override
	public List<String> listConnection(String user) {
		return fileProfile.listProfilesName(user);
	}

	@Override
	public boolean save(String user, ConnectionInfo info) {
		try {
			fileProfile.saveConnectionInfo(user, info);
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
		return false;
	}

}
