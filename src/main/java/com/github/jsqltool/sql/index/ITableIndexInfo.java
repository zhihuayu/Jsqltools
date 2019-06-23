package com.github.jsqltool.sql.index;

import java.util.List;
import java.util.Map;

public interface ITableIndexInfo {

	List<Map<String, String>> getIndexInfo(String user, String tableName);

}
