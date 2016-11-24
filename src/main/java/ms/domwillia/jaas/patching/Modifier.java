package ms.domwillia.jaas.patching;

import javassist.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class Modifier
{
	private final Map<String, GeneratedCode> code;
	private final ClassPool pool;

	public Modifier()
	{
		pool = ClassPool.getDefault();
		code = new HashMap<>();

		if (!GeneratedCode.parseFile("Testing", code))
			throw new IllegalStateException("Failed to load test code");

		System.out.printf("Loaded %d code snippets\n", code.size());
	}

	public byte[] modify(String className, Predicate<CtMethod> poisonPredicate)
	{
		try
		{
			PlaceholderContext context = new PlaceholderContext(code);
			GeneratedCode preCode = code.get("pre");

			CtClass original = pool.get(className);
			Arrays.stream(original.getDeclaredMethods())
				.filter(poisonPredicate)
				.forEach(method ->
				{
					context.set(PlaceholderContext.Key.METHOD_NAME, method.getName());
					context.set(PlaceholderContext.Key.METHOD_LONG_NAME, method.getLongName());

					System.out.printf("Poisoning %s ... ", method.getLongName());
					try
					{
						if (preCode != null)
						{
							method.insertBefore(preCode.getCode(context));
						}

						System.out.printf("done\n");
					} catch (CannotCompileException e)
					{
						System.out.printf("FAILED (%s)\n", e.getReason());
					}
				});

			try
			{
				return original.toBytecode();
			} catch (CannotCompileException | IOException e)
			{
				e.printStackTrace();
			}

		} catch (NotFoundException e)
		{
			System.err.printf("Failed to find class '%s'\n", className);
		}
		return null;
	}

}
