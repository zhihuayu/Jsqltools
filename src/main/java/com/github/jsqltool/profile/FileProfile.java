package com.github.jsqltool.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.jsqltool.entity.ConnectionInfo;
import com.github.jsqltool.exception.JsqltoolParamException;
import com.github.jsqltool.exception.ProfileParserException;

public class FileProfile {

	private final String filePath;
	private final String fileSeparator = File.separator;
	private final String profilePorpPrefix;

	private final static String filePathKey = "jsqltool.profiles.filePath";
	private final static String defaultFilePath = "dbProfile";

	public FileProfile(Properties prop) {
		if (prop == null) {
			throw new JsqltoolParamException("Jsqltool配置文件不存在！!");
		}
		// 设置文件路径
		String filePath = prop.getProperty(filePathKey);
		if (StringUtils.isBlank(filePath)) {
			this.filePath = "/" + defaultFilePath;
		} else {
			this.filePath = "/" + filePath.trim();
		}
		// 设置profilePorpPrefix，属性的前缀名称
		String prefix = prop.getProperty("jsqltool.profiles.prefix");
		if (StringUtils.isBlank(prefix)) {
			this.profilePorpPrefix = "jdbc.properties";
		} else {
			this.profilePorpPrefix = prefix;
		}

	}

	public List<String> listProfilesName(String user) {
		List<String> names = new ArrayList<>();
		File rootPathFile = getRootPathFile(user);
		if (rootPathFile != null)
			addFiles(names, rootPathFile);
		return names;
	}

	public List<ConnectionInfo> listConnectionInfo(String user) {
		List<String> names = listProfilesName(user);
		List<ConnectionInfo> result = new ArrayList<>();
		for (String name : names) {
			try {
				result.add(loadConnectionInfo(user, name));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	private File getRootPathFile(String user) {
		File userHomeRootFile = getUserHomeRootFile(user);
		if (userHomeRootFile != null)
			return userHomeRootFile;
		return getClassPathRootFile(user);
	}

	private File getUserHomeRootFile(String user) {
		String home = System.getProperty("user.home");
		if (StringUtils.isNotBlank(home)) {
			String relativePath = getRelativePath(user);
			return new File(home, relativePath);
		}
		return null;
	}

	private File getClassPathRootFile(String user) {
		URL resource = FileProfile.class.getResource("/");
		if (resource != null) {
			String relativePath = getRelativePath(user);
			return new File(resource.getFile(), relativePath);
		}
		return null;
	}

	private String getRelativePath(String user) {
		if (StringUtils.isNotBlank(user)) {
			return filePath + fileSeparator + user;
		} else {
			return filePath;
		}
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
			File file = new File(getRootPathFile(userName), name + ".properties");
			in = new FileInputStream(file);
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
		info.setProp(convertToProperties(properties));
		return info;
	}

	private Properties convertToProperties(Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		Set<String> stringPropertyNames = properties.stringPropertyNames();
		Properties prop = new Properties();
		for (String key : stringPropertyNames) {
			if (key.startsWith(profilePorpPrefix)) {
				String value = properties.getProperty(key);
				String ck = key.substring(profilePorpPrefix.length() + 1);
				if (StringUtils.isNoneBlank(ck, value))
					prop.setProperty(ck.trim(), value.trim());
			}
		}
		return prop;
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
		// 设置属性
		Properties prop = info.getProp();
		if (prop != null && !prop.isEmpty()) {
			Set<String> stringPropertyNames = prop.stringPropertyNames();
			for (String key : stringPropertyNames) {
				String v = prop.getProperty(key);
				String k = profilePorpPrefix + "." + key.trim();
				if (StringUtils.isNoneBlank(v, k)) {
					properties.setProperty(k.trim(), v.trim());
				}
			}
		}
		File file = getRootPathFile(owner);
		if (!file.exists()) {
			file.mkdirs();
		}
		// 删除老版的文件
		if (StringUtils.isNotBlank(oldConnectionName)) {
			File oldFile = new File(file, oldConnectionName + ".properties");
			if (oldFile.exists()) {
				oldFile.delete();
			}
		}
		File nfile = new File(file, name + ".properties");
		try (FileOutputStream out = new FileOutputStream(nfile);) {
			properties.store(out, "数据库配置文件");
		}
	}

	public boolean delete(String user, String connectionName) {
		if (StringUtils.isBlank(connectionName)) {
			throw new ProfileParserException("connectionName参数不能为空");
		}
		File file = new File(getRootPathFile(user), connectionName.trim() + ".properties");
		if (file.exists()) {
			return file.delete();
		} else {
			throw new ProfileParserException("连接信息不存在！");
		}
	}

}
