package ms.domwillia.jaas.patching;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderContext
{
	private final Map<Key, String> map;
	private final Map<String, GeneratedCode> codeLookup;

	public PlaceholderContext(Map<String, GeneratedCode> codeLookup)
	{
		this.codeLookup = codeLookup;
		map = new HashMap<>();
	}

	public void set(Key key, String value)
	{
		map.put(key, value);
	}

	public String getArg(String key)
	{
		Key k = Key.get(key);
		if (k == null)
			throw new IllegalArgumentException("Unrecognised argument '" + key + "'");

		String value = map.getOrDefault(k, null);
		if (value == null)
			throw new IllegalStateException("Argument '" + key + "' has not been set");

		return value;
	}

	public String getCode(String key)
	{
		GeneratedCode code = codeLookup.getOrDefault(key, null);
		if (code == null)
			throw new IllegalArgumentException("Code snippet '" + key + "' has not been defined");

		return code.getCode(null);
	}

	public enum Key
	{
		METHOD_NAME("methodName"),
		METHOD_LONG_NAME("methodLongName");

		private final String name;

		Key(String name)
		{
			this.name = name;
		}

		public static Key get(String name)
		{
			for (Key key : values())
				if (key.name.equals(name))
					return key;

			return null;
		}
	}


}
