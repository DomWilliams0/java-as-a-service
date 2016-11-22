package patching;

import javassist.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Modifier
{
	private ClassPool pool;

	public Modifier()
	{
		pool = ClassPool.getDefault();

		List<GeneratedCode> code = new ArrayList<>();
		if (!GeneratedCode.parseFile("Testing", code))
			throw new IllegalStateException("Failed to load test code");
	}

	public byte[] modify(String className, String[] methodsToPoison)
	{
		try
		{
			CtClass original = pool.get(className);

			for (String name : methodsToPoison)
			{
				CtMethod[] overloadedMethods = original.getDeclaredMethods(name);
				for (CtMethod method : overloadedMethods)
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
				}
			}
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
