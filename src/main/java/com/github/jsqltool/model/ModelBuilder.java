package com.github.jsqltool.model;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsqltool.exception.JsqltoolParamException;

public class ModelBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ModelBuilder.class);

	// 自定义Model的属性名称的key值
	private static final String customerClassPropKey = "jsqltool.model.customeClass";
	// 内建模式的选择的key值
	private static final String internalModelPropKey = "jsqltool.model";

	private ModelBuilder() {
	}

	public static IModel builder(Properties prop) {
		String customerModel = prop.getProperty(customerClassPropKey);
		if (StringUtils.isBlank(customerModel)) {
			// 内置模式
			return builderInternal(prop);
		} else {
			// 自定义模式
			return builderCustomer(customerModel, prop);
		}
	}

	/**
	 * 
	* @author yzh
	* @date 2019年10月10日
	* @Description: 以内建模式的方式来实例化，其默认为ProfileModel即配置文件的模式
	 */
	private static IModel builderInternal(Properties prop) {
		String m = prop.getProperty(internalModelPropKey);
		if (StringUtils.equalsIgnoreCase(m, "databaseProfile")) {
			logger.info("使用内建的DatabaseModel来管理连接信息");
			return new DatabaseModel(prop);
		} else {
			logger.info("使用内建的ProfileModel来管理连接信息");
			return new ProfileModel(prop);
		}
	}

	/**
	 * 
	* @author yzh
	* @date 2019年10月10日
	* @Description: 以自定义模式的的方式来实例化，其默认为ProfileModel即配置文件的模式
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static IModel builderCustomer(String customerModel, Properties prop) {
		try {
			Class clazz = Class.forName(customerModel);
			if (IModel.class.isAssignableFrom(clazz)) {
				Constructor constructor = null;
				try {
					constructor = clazz.getConstructor(Properties.class);
				} catch (NoSuchMethodException | SecurityException e) {
				}
				try {
					if (constructor == null) {
						logger.info("使用自定义的{}来管理连接信息", clazz.getName());
						return (IModel) clazz.newInstance();
					} else {
						logger.info("使用自定义的{}来管理连接信息", clazz.getName());
						return (IModel) constructor.newInstance(prop);
					}
				} catch (Exception e) {
					throw new JsqltoolParamException(customerModel + "实例化失败！");
				}

			} else {
				throw new JsqltoolParamException(customerModel + "必须实现com.github.jsqltool.model.IModel接口！");
			}
		} catch (ClassNotFoundException e) {
			throw new JsqltoolParamException(customerModel + "不存在", e);
		}
	}
}
