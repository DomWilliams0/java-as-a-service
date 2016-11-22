package patching;

import javassist.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Modifier
{
	private ClassPool pool;

	public Modifier()
	{
		pool = ClassPool.getDefault();

		List<GeneratedCode> code = new ArrayList<>();
		if (!GeneratedCode.parseFile("Testing", code))
			throw new IllegalStateException("Failed to load test code");
		System.out.printf("Loaded %d code snippets\n", code.size());
	}

	public byte[] modify(String className, Predicate<CtMethod> poisonPredicate)
	{
		try
		{
			CtClass original = pool.get(className);
			Arrays.stream(original.getDeclaredMethods())
				.filter(poisonPredicate)
				.forEach(method ->
				{
					System.out.printf("Poisoning %s ... ", method.getLongName());
					try
					{
						String printObjectO = "o.toString() + \":\" + o.getClass().getName()";
						method.insertBefore(
							"{" +
								"System.out.print(\">>> " + method.getName() + "(\");" +
								"Object[] args = $args;" +
								"Object o = args.length > 0 ? args[0] : null;" +
								"if (o != null) System.out.print(" + printObjectO + ");" +
								"for (int i = 1; i < args.length; i++)" +
								"{" +
								"Object o = args[i];" +
								"System.out.print(\", \" + " + printObjectO + ");" +
								"}" +
								"System.out.println(\")\");" +
								"}"
						);

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
