package com.github.jsqltool.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.exception.JsqltoolBuildException;

/**
 * 
 * @author yzh
 * @date 2019年10月10日
 * 配置文件读取器，用于加载配置文件
 */
public class ConfigPropertiesReader {
	private static final Logger logger = LoggerFactory.getLogger(ConfigPropertiesReader.class);
	// 配置文件的路径（classpath下）
	private static final String configPath = "/jsqltool.properties";
	// 该实例是单例的
	private static volatile Properties prop = null;

	private ConfigPropertiesReader() {
	}

	public static Properties loadProperties() {
		if (prop == null) {
			synchronized (ConfigPropertiesReader.class) {
				if (prop == null) {
					prop = loadNewProperties();
				}
			}
		}
		logger.info("配置文件加载成功！");
		return (Properties) prop.clone();
	}

	private static Properties loadNewProperties() {
		try (InputStream config = JsqltoolBuilder.class.getResourceAsStream(configPath)) {
			Properties prop = new Properties();
			prop.load(config);
			return prop;
		} catch (IOException e) {
			throw new JsqltoolBuildException("JsqltoolBuilder创建失败", e);
		}
	}

}
