package com.github.jsqltool.sql.excuteCall.impl;

import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.sql.excuteCall.AbstractExecuteCallLifeCycle;

public class DefaultExecuteCallHandler extends AbstractExecuteCallLifeCycle {

	@Override
	public boolean support(DBType dbType) {
		return true;
	}

}
