package schevo.server.space;

import java.util.regex.Pattern;

import schevo.server.Config;
import schevo.server.common.Utils;

/**
 * Utilities for space
 * 
 * @author tomecode.com
 *
 */
public final class SpaceName {

	private static final Pattern REGEX_NON_ALPHA_NUMERIC = Pattern.compile("[^A-Za-z0-9]");
	private static final Pattern REGEX_SLASH = Pattern.compile("/+");

	private static final String[] EMPTY_STRING_ARRAY = {};

	private SpaceName() {

	}

	public static final String toName(String name) {
		name = Utils.strOrNull(name);
		if (name == null) {
			return null;
		}
		name = REGEX_NON_ALPHA_NUMERIC.matcher(name).replaceAll("-").toLowerCase();
		if (name.length() > Config.SPACE_NAME_MAX_LENGTH) {
			return name.substring(0, Config.SPACE_NAME_MAX_LENGTH);
		}
		return name;
	}

	public final static String[] splitRef(String spaceRef) {
		spaceRef = Utils.strOrNull(spaceRef);
		if (spaceRef == null) {
			return EMPTY_STRING_ARRAY;
		}
		return REGEX_SLASH.split(spaceRef, 0);
	}
}
