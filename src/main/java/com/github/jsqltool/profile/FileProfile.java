package com.github.jsqltool.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.exception.CannotFindProfileException;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.exception.ProfileParserException;

public class FileProfile {

	private final String filePath;

	public FileProfile(Properties prop) {
		if (prop == null) {
			throw new JsqltoolParamException("Jsqltool配置文件不存在！!");
		}
		String filePath = prop.getProperty("jsqltool.profiles.filePath");
		if (StringUtils.isBlank(filePath)) {
			this.filePath = "/dbProfile";
		} else {
			this.filePath = "/" + filePath.trim();
		}
	}

	public List<String> listProfilesName(String user) {
		URL resource = null;
		if (StringUtils.isBlank(user)) {
			resource = FileProfile.class.getResource(filePath);
		} else {
			resource = FileProfile.class.getResource(filePath + "/" + user);
		}
		List<String> names = new ArrayList<>();
		if (resource != null) {
			File file = new File(resource.getFile());
			addFiles(names, file);
		}
		return names;
	}

	private void addFiles(List<String> names, File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			if (list != null) {
				for (File f : list) {
					addFile(names, f);
				}
			}
		} else {
			addFile(names, file);
		}

	}

	private void addFile(List<String> names, File file) {
		if (!file.isDirectory()) {
			String name = file.getName();
			int point = name.lastIndexOf(".");
			if (point > -1) {
				names.add(name.substring(0, point));
			}
		}
	}

	public ConnectionInfo loadConnectionInfo(String userName, String name) throws IOException {
		InputStream in = null;
		try {
			if (StringUtils.isBlank(userName)) {
				in = this.getClass().getResourceAsStream(filePath + "/" + name + ".properties");
			} else {
				in = this.getClass().getResourceAsStream(filePath + "/" + userName + "/" + name + ".properties");
			}
			if (in == null) {
				throw new CannotFindProfileException("不能找到对应的属性文件：" + name + ".properties");
			}
			Properties properties = new Properties();
			properties.load(in);
			ConnectionInfo info = convertToConnectionInfo(name, properties);
			return info;
		} finally {
			if (in != null)
				in.close();
		}
	}

	private ConnectionInfo convertToConnectionInfo(String name, Properties properties) {
		String className = (String) properties.get("jdbc.className");
		String url = (String) properties.get("jdbc.url");
		String username = (String) properties.get("jdbc.username");
		String password = (String) properties.get("jdbc.password");
		if (!StringUtils.isNoneBlank(name, className, url, username, password)) {
			throw new ProfileParserException("参数异常");
		}
		ConnectionInfo info = new ConnectionInfo();
		info.setName(name);
		info.setDriverClassName(className);
		info.setUrl(url);
		info.setPassword(password);
		info.setUserName(username);
		return info;
	}

	public void saveConnectionInfo(String owner, String oldConnectionName, ConnectionInfo info) throws IOException {
		Properties properties = new Properties();
		String name = info.getName();
		String className = info.getDriverClassName();
		String url = info.getUrl();
		String username = info.getUserName();
		String password = info.getPassword();
		if (!StringUtils.isNoneBlank(name, className, url, username, password)) {
			throw new ProfileParserException("参数异常");
		}
		properties.put("jdbc.className", className);
		properties.put("jdbc.url", url);
		properties.put("jdbc.username", username);
		properties.put("jdbc.password", password);

		URL resource = this.getClass().getResource("/");
		File file = new File(resource.getFile(), filePath);
		if (StringUtils.isNotBlank(owner)) {
			file = new File(file, owner);
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		// 删除老版的文件
		if (StringUtils.isNotBlank(oldConnectionName)) {
			File oldFile = new File(file, oldConnectionName + ".properties");
			if (oldFile.exists()) {
				oldFile.deleteOnExit();
			}
		}
		File nfile = new File(file, name + ".properties");
		try (FileOutputStream out = new FileOutputStream(nfile);) {
			properties.store(out, "数据库配置文件");
		}
	}

	public boolean delete(String user, String connectionName) {
		URL resource = null;
		if (StringUtils.isBlank(user)) {
			resource = FileProfile.class.getResource(filePath);
		} else {
			resource = FileProfile.class.getResource(filePath + "/" + user);
		}
		if (StringUtils.isBlank(connectionName)) {
			throw new ProfileParserException("connectionName参数不能为空");
		}
		File file = new File(resource.getFile(), connectionName.trim() + ".properties");
		if (file.exists()) {
			return file.delete();
		} else {
			throw new ProfileParserException("连接信息不存在！");
		}
	}

}
