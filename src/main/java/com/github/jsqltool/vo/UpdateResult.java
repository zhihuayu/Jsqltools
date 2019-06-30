package com.github.jsqltool.vo;

/**
 * 数据更新的结果
 * 
 * @author yzh
 * @date 2019年6月28日
 */
public class UpdateResult {

	public final static int WARN = 300; // 警告消息
	public final static int OK = 200;
	public final static int SERVER_ERROR = 500;

	private Integer code;
	private String msg;
	private Long time;
	private Integer effectRows;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getEffectRows() {
		return effectRows;
	}

	public void setEffectRows(Integer effectRows) {
		this.effectRows = effectRows;
	}

}
