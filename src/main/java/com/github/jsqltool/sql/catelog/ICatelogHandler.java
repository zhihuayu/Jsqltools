package com.github.jsqltool.sql.catelog;

import java.sql.Connection;
import java.util.List;

/**
 * Catelog的处理类，一般来说catelog的值为空，但是对于MySQL来说该值就是数据库的名称
 * 
 * @author yzh
 * @date 2019年6月17日
 */
public interface ICatelogHandler {

	/**
	 * 根据用户获取所有的catelog
	 * 
	 * @author yzh
	 * @date 2019年6月17日
	 */
	List<String> list(Connection connection);

	boolean support(Connection connection);

}
