package com.github.jsqltool.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.result.SqlResult;
import com.github.jsqltool.result.SqlResult.Column;
import com.github.jsqltool.result.SqlResult.Record;

public class SqlResultUtil {

	private SqlResultUtil() {
	}

	/**
	* @author yzh
	* @date 2019年9月2日
	* @Description: T对象中的字段只支持简单的诸如String,Integer，Boolean,Date等类型的，不支持递归操作
	 */
	public static <T> SqlResult ObjectToSqlResult(List<T> obj) throws Exception {
		SqlResult result = new SqlResult();

		if (obj == null || obj.isEmpty()) {
			result.setCount(0);
		} else {
			result.setCount(obj.size());
			BeanInfo beanInfo = Introspector.getBeanInfo(obj.get(0).getClass(),Object.class);
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			List<Column> listColumns = listColumns(propertyDescriptors);
			List<Record> records = new ArrayList<>();
			for (int i = 0; i < obj.size(); i++) {
				T t = obj.get(i);
				Record record = new Record();
				List<Object> values = new ArrayList<Object>();
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					Object invoke = propertyDescriptor.getReadMethod().invoke(t);
					if (invoke != null && invoke instanceof Date) {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String str = format.format(invoke);
						values.add(str);
					} else if (invoke != null) {
						values.add(invoke.toString());
					} else {
						values.add("");
					}
				}
				record.setValues(values);
				records.add(record);

			}
			result.setColumns(listColumns);
			result.setRecords(records);
		}
		return result;
	}

	private static List<Column> listColumns(PropertyDescriptor[] propertyDescriptors) {
		List<Column> result = new ArrayList<>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			Column column = new Column();
			String name = propertyDescriptor.getName();
			String className = propertyDescriptor.getPropertyType().getName();
			column.setColumnName(name);
			column.setAlias(name);
			if (className.equalsIgnoreCase("date")) {
				column.setDataType(JdbcType.DATE.TYPE_CODE);
				column.setTypeName(JdbcType.DATE.name());
			}
			result.add(column);
		}
		return result;
	}

}
