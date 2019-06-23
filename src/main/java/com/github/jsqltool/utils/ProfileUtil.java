package com.github.jsqltool.utils;

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
import com.github.jsqltool.exception.ProfileParserException;

public class ProfileUtil {

	private static final String filePath = "/dbProfile";

	public static List<String> listProfilesName(String user) {
		URL resource = null;
		if (StringUtils.isBlank(user)) {
			resource = ProfileUtil.class.getResource(filePath);
		} else {
			resource = ProfileUtil.class.getResource(filePath + "/" + user);
		}
		List<String> names = new ArrayList<>();
		if (resource != null) {
			File file = new File(resource.getFile());
			addFiles(names, file);
		}
		return names;
	}

	private static void addFiles(List<String> names, File file) {
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

	private static void addFile(List<String> names, File file) {
		if (!file.isDirectory()) {
			String name = file.getName();
			int point = name.lastIndexOf(".");
			if (point > -1) {
				names.add(name.substring(0, point));
			}
		}
	}

	public static ConnectionInfo loadConnectionInfo(String userName, String name) throws IOException {
		InputStream in = null;
		if (StringUtils.isBlank(userName)) {
			in = ProfileUtil.class.getResourceAsStream(filePath + "/" + name + ".properties");
		} else {
			in = ProfileUtil.class.getResourceAsStream(filePath + "/" + userName + "/" + name + ".properties");
		}
		if (in == null) {
			throw new CannotFindProfileException("不能找到对应的属性文件：" + name + ".properties");
		}
		Properties properties = new Properties();
		properties.load(in);
		ConnectionInfo info = convertToConnectionInfo(properties);
		return info;
	}

	private static ConnectionInfo convertToConnectionInfo(Properties properties) {
		String name = (String) properties.get("jdbc.name");
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

	public static void saveConnectionInfo(String owner, ConnectionInfo info) throws IOException {
		Properties properties = new Properties();
		String name = info.getName();
		String className = info.getDriverClassName();
		String url = info.getUrl();
		String username = info.getUserName();
		String password = info.getPassword();
		if (!StringUtils.isNoneBlank(name, className, url, username, password)) {
			throw new ProfileParserException("参数异常");
		}
		properties.put("jdbc.name", name);
		properties.put("jdbc.className", className);
		properties.put("jdbc.url", url);
		properties.put("jdbc.username", username);
		properties.put("jdbc.password", password);

		URL resource = ProfileUtil.class.getResource("/");
		File file = new File(resource.getFile(), filePath);
		if (StringUtils.isNotBlank(owner)) {
			file = new File(file, owner);
		}
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(file, name + ".properties");
		properties.store(new FileOutputStream(file), "数据库配置文件");
	}

	public static void main(String[] args) throws IOException {

		ConnectionInfo builderDefaultOracleInfo = ConnectionInfo.builderDefaultOracleInfo();
		ProfileUtil.saveConnectionInfo(null, builderDefaultOracleInfo);
		ConnectionInfo mysql = ConnectionInfo.builderDefaultMysqlInfo();
		ProfileUtil.saveConnectionInfo(null, mysql);

//		System.out.println(listProfilesName("user1"));
//
//		List<String> listProfilesName = listProfilesName("user1");
//		for (String name : listProfilesName) {
//			ConnectionInfo loadConnectionInfo = loadConnectionInfo("user1", name);
//			System.out.println(loadConnectionInfo);
//		}

	}

}
