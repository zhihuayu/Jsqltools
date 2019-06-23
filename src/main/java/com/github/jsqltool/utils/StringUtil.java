package com.github.jsqltool.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static boolean isFind(String str, String regex) {
		Matcher matcher = getMatcher(str, regex);
		return matcher.find();
	}

	public static String findRegStr(String str, String regex) {
		return findRegStr(str, regex, 0);
	}

	public static String findRegStr(String str, String regex, int group) {
		Matcher matcher = getMatcher(str, regex);
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}

	private static Matcher getMatcher(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(str);
	}

	/**
	 * <p>
	 * Checks if a CharSequence is empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtil.isEmpty("") = true
	 * StringUtil.isEmpty(" ") = false
	 * StringUtil.isEmpty("bob") = false
	 * StringUtil.isEmpty("  bob  ") = false *
	 * </pre>
	 */

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * Checks if a CharSequence is whitespace, empty ("") or null.
	 * 
	 * <pre>
	 * StringUtil.isBlank(null)      = true
	 * StringUtil.isBlank("")        = true
	 * StringUtil.isBlank(" ")       = true
	 * StringUtil.isBlank("bob")     = false
	 * StringUtil.isBlank("  bob  ") = false
	 * </pre>
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

}
