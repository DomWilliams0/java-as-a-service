package ms.domwillia.jaas.patching;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratedCode
{
	class Placeholder
	{
		public final String name;
		public final int start, end;

		Placeholder(String name, int start, int end)
		{
			this.name = name;
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString()
		{
			return "Placeholder{" +
				"name='" + name + '\'' +
				", start=" + start +
				", end=" + end +
				'}';
		}


		public int substitute(StringBuilder code, String replacement, int currentOffset)
		{
			code.replace(start + currentOffset, end + currentOffset, replacement);

			// how much to offset the offset; the offset-offset, if you will
			return replacement.length() - (end - start);
		}
	}

	private static final Pattern PATTERN_TOKEN =
		Pattern.compile("(//)\\s*#([A-Z]+)(?: )?([a-zA-Z0-9]+)?\\s*(\\n)");

	private static final Pattern PATTERN_PLACEHOLDER_ARG =
		Pattern.compile("#\\$([a-zA-Z0-9]+)");

	private static final Pattern PATTERN_PLACEHOLDER_CODE =
		Pattern.compile("#\\(([a-zA-Z0-9]+)\\)#");

	private final String identifier;
	private final StringBuilder code;

	private final List<Placeholder> argPlaceholders;
	private final List<Placeholder> codePlacehoders;

	private GeneratedCode(String identifier, StringBuilder code)
	{
		this.identifier = identifier;
		this.code = code;
		this.codePlacehoders = new ArrayList<>();
		this.argPlaceholders = new ArrayList<>();

		discoverPlaceholders();
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getCode(PlaceholderContext context)
	{
		StringBuilder localCode = new StringBuilder(code);

		if (context != null)
		{
			int offset = 0;
			for (Placeholder argPH : argPlaceholders)
			{
				try
				{
					String replacement = context.getArg(argPH.name);
					offset += argPH.substitute(localCode, replacement, offset);

				} catch (IllegalArgumentException | IllegalStateException e)
				{
					System.err.println(e.getMessage());
				}
			}

			for (Placeholder codePH : codePlacehoders)
			{
				try
				{
					String replacement = context.getCode(codePH.name);
					offset += codePH.substitute(localCode, replacement, offset);

				} catch (IllegalArgumentException | IllegalStateException e)
				{
					System.err.println(e.getMessage());
				}

			}
		}

		return localCode.toString();
	}

	@Override
	public String toString()
	{
		return "GeneratedCode{" +
			"identifier='" + identifier + '\'' +
			", code='" + code + '\'' +
			'}';
	}

	private void discoverPlaceholders()
	{
		discoverPlaceholders(PATTERN_PLACEHOLDER_ARG, argPlaceholders, 2, 0);
		discoverPlaceholders(PATTERN_PLACEHOLDER_CODE, codePlacehoders, 2, 2);
	}

	private void discoverPlaceholders(Pattern pattern, List<Placeholder> out, int startOffset, int endOffset)
	{
		Matcher matcher = pattern.matcher(code);
		while (matcher.find())
		{
			String token = matcher.group(1);
			Placeholder placeholder = new Placeholder(token,
				matcher.start(1) - startOffset,
				matcher.end(1) + endOffset);
			out.add(placeholder);
		}
	}

	public static boolean parseFile(String fileName, Map<String, GeneratedCode> parsedCode)
	{
		try
		{
			URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName + ".java");
			if (resource == null)
				throw new FileNotFoundException("File not found");

			String file = new String(Files.readAllBytes(Paths.get(resource.getPath())));
			Matcher matcher = PATTERN_TOKEN.matcher(file);

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
				StringBuilder code = new StringBuilder((end - start) + 4);
				code.append("{\n");
				code.append(file.substring(start, end));
				code.append("\n}");

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
