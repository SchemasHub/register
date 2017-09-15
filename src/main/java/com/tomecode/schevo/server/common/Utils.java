package com.tomecode.schevo.server.common;

import java.util.UUID;
import java.util.regex.Pattern;

public final class Utils {

	//private static Pattern REGEX_SLASH = Pattern.compile("/+");

	public static final String randomId() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static final String strOrNullStr(String val) {
		val = strOrNull(val);
		if (val != null && "null".equals(val)) {
			return null;
		}
		return val;
	}

	public static final String strOrNull(String val) {
		if (val == null) {
			return val;
		}
		val = val.trim();
		if (val.length() == 0) {
			return null;
		}
		return val;
	}

	

}
