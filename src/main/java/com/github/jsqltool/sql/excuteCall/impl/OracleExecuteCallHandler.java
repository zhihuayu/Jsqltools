package com.github.jsqltool.sql.excuteCall.impl;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.github.jsqltool.config.JsqltoolBuilder;
import com.github.jsqltool.enums.DBType;
import com.github.jsqltool.enums.JdbcType;
import com.github.jsqltool.param.ProcedureParam;
import com.github.jsqltool.param.ProcedureParam.P_Param;
import com.github.jsqltool.sql.excuteCall.AbstractExecuteCallLifeCycle;

public class OracleExecuteCallHandler extends AbstractExecuteCallLifeCycle {

	@Override
	protected String beforeExecuteCall(Connection c, ProcedureParam param) throws SQLException {
		String oracleDbms = "  begin\r\n" + "dbms_output.enable();\r\n" + "end;";
		try (CallableStatement call = c.prepareCall(oracleDbms)) {
			call.execute();
		}
		return "";
	}

	@Override
	protected String afterExecuteCall(Connection c, ProcedureParam param) throws SQLException {
		String oracleDbms = " declare \r\n" + "				   num number := 1000;\r\n" + "				 begin\r\n"
				+ "				   dbms_output.get_lines(?,num);\r\n" + "dbms_output.disable();\r\n"
				+ "				 end;";
		StringBuilder sb = new StringBuilder();
		try (CallableStatement call = c.prepareCall(oracleDbms)) {
			call.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
			call.execute();
			Array array = null;
			try {
				array = call.getArray(1);
				if (array == null)
					return "";
				else {
					Object[] array2 = (Object[]) array.getArray();
					if (array2 != null && array2.length > 0) {
						for (Object obj : array2) {
							if (obj != null) {
								sb.append("\n");
								sb.append(obj);
							}
						}
					}
					if (sb.length() > 0) {
						sb.insert(0, "\r\noutput:");
						return sb.toString();
					}
				}
				return "";
			} finally {
				if (array != null)
					array.free();
			}
		}
	}

	@Override
	public boolean support(DBType dbType) {
		return dbType == DBType.ORACLE_TYPE;
	}

	public static void main(String[] args) throws SQLException {
		JsqltoolBuilder builder = JsqltoolBuilder.builder();
		try (Connection connect = builder.connect("测试Oracle");) {
			ProcedureParam param = new ProcedureParam();
			param.setSchema("scott");
			param.setProcedureName("f_test");
			param.setReturnType(JdbcType.NUMERIC);
			List<P_Param> fps = new ArrayList<ProcedureParam.P_Param>();
			P_Param f_n = new P_Param();
			f_n.setDataType(JdbcType.NUMERIC);
			f_n.setParamIndex(1);
			f_n.setType("IN");
			f_n.setValue(16);
			fps.add(f_n);
			P_Param f_out = new P_Param();
			f_out.setDataType(JdbcType.NUMERIC);
			f_out.setParamIndex(2);
			f_out.setType("IN OUT");
			f_out.setValue(12);
			fps.add(f_out);
			param.setParams(fps);

			OracleExecuteCallHandler call = new OracleExecuteCallHandler();
			String executeCall = call.executeCall(connect, param);
			System.out.println(executeCall);

		}

	}

}
