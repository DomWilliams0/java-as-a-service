package ms.domwillia.jaas.patching;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratedCode
{
	private static final Pattern capturePattern = Pattern.compile("(//)\\s*#([A-Z]+)(?: )?([a-zA-Z0-9]+)?\\s*(\\n)");

	private final String identifier;
	private final String code;

	private GeneratedCode(String identifier, String code)
	{
		this.identifier = identifier;
		this.code = code;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getCode()
	{
		return code;
	}

	@Override
	public String toString()
	{
		return "GeneratedCode{" +
			"identifier='" + identifier + '\'' +
			", code='" + code + '\'' +
			'}';
	}

	public static boolean parseFile(String fileName, Map<String, GeneratedCode> parsedCode)
	{
		try
		{
			URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName + ".java");
			if (resource == null)
				throw new FileNotFoundException("File not found");

			String file = new String(Files.readAllBytes(Paths.get(resource.getPath())));
			Matcher matcher = capturePattern.matcher(file);

			int start = 0, end = 0;
			String identifier = null;

			while (matcher.find())
			{
				String match = matcher.group(2);
				boolean matchesBegin = match.equals("BEGIN");
				boolean matchesEnd = match.equals("END");

				// uh oh
				if (!matchesBegin && !matchesEnd)
					throw new IllegalStateException("Invalid token " + match);

				// wrong order
				if ((matchesBegin ? start : end) != 0)
					throw new IllegalStateException("Repeat token " + match);
				if (matchesEnd && start == 0)
					throw new IllegalStateException("Unexpected closing token");

				// set marker
				if (matchesBegin)
				{
					identifier = matcher.group(3);
					if (identifier == null)
						throw new IllegalStateException("Missing identifier");

					start = matcher.start(4);
				} else
				{
					end = matcher.start(1);
				}

				// both markers not yet set
				if (start == 0 || end == 0)
					continue;

				// deal with code
				String code = "{\n" + file.substring(start, end) + "\n}";
				parsedCode.put(identifier, new GeneratedCode(identifier, code));

				// reset markers
				start = end = 0;
			}

			if (start != 0)
				throw new IllegalStateException("Code tag not closed");

			return true;

		} catch (IOException e)
		{
			System.err.printf("Failed to read code file \"%s\": %s\n", fileName, e.getMessage());
			return false;
		}
	}

}
