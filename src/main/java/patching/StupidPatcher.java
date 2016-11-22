package patching;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class StupidPatcher implements ClassFileTransformer
{
	private Modifier modifier;

	private String classToPoison;
	private String[] methodsToPoison;

	public StupidPatcher()
	{
		modifier = new Modifier();

//		classToPoison = "java/lang/String";
		methodsToPoison = new String[]{"substring", "charAt"};
	}

	public static void premain(String agentArgument, final Instrumentation instrumentation)
	{
		StupidPatcher patcher = new StupidPatcher();
		instrumentation.addTransformer(patcher, true);

		try
		{
			instrumentation.retransformClasses(System.class);
		} catch (UnmodifiableClassException e)
		{
			e.printStackTrace();
		}

		Arrays.stream(instrumentation.getAllLoadedClasses())
			.filter(instrumentation::isModifiableClass)
			.forEach(classes ->
			{
				try
				{
					instrumentation.retransformClasses(classes);
				} catch (UnmodifiableClassException e)
				{
					e.printStackTrace();
				}
			});

	}

	public byte[] transform(final ClassLoader loader, String className,
	                        final Class classBeingRedefined, final ProtectionDomain protectionDomain,
	                        final byte[] classfileBuffer) throws IllegalClassFormatException
	{
		if (className.equals(classToPoison))
		{
			return modifier.modify(className.replace("/", "."), methodsToPoison);
		}

		return null;
	}
}
