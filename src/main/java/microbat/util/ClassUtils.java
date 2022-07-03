/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package microbat.util;

/**
 * @author LLT
 *
 */
public class ClassUtils {
	private ClassUtils() {}

	public static Class<?> getArrayContentType(Class<?> type) {
		Class<?> contentType = type;
		while (contentType.isArray()) {
			contentType = contentType.getComponentType();
		}
		if (contentType == type) {
			return null;
		}
		return contentType;
	}
	
	/**
	 * The case which this function is missing to handle: Non-public top level class
	 * */
	public static String getCompilationUnitForSimpleCase(String className) {
		if (className.contains("$")) {
			return className.substring(0, className.indexOf("$"));
		} else {
			return className;
		}
	}

}
