package ms.domwillia.jaas.patching;

import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.function.Predicate;

public class StupidPatcher implements ClassFileTransformer
{
	private Modifier modifier;

	private Predicate<String> patchedClassPredicate;
	private Predicate<CtMethod> poisonedMethodPredicate;

	public StupidPatcher(Predicate<String> patchedClassPredicate, Predicate<CtMethod> poisonedMethodPredicate)
	{
		this.patchedClassPredicate = patchedClassPredicate;
		this.poisonedMethodPredicate = poisonedMethodPredicate;
		this.modifier = new Modifier();
	}

	public static void premain(String agentArgument, final Instrumentation instrumentation)
	{
		Predicate<String> patchedClassPredicate = new PatchedClassPredicate();
		Predicate<CtMethod> poisonedMethodPredicate = new PoisonedMethodPredicate();

		StupidPatcher patcher = new StupidPatcher(patchedClassPredicate, poisonedMethodPredicate);
		instrumentation.addTransformer(patcher, true);

		Arrays.stream(instrumentation.getAllLoadedClasses())
			.filter(instrumentation::isModifiableClass)
			.forEach(classes ->
			{
				try
				{
					instrumentation.retransformClasses(classes);
				} catch (Exception e)
				 {
					 e.printStackTrace();
				 }
			});

	}

	public byte[] transform(final ClassLoader loader, String className,
	                        final Class classBeingRedefined, final ProtectionDomain protectionDomain,
	                        final byte[] classfileBuffer) throws IllegalClassFormatException
	{
		if (patchedClassPredicate.test(className))
		{
			return modifier.modify(className.replace("/", "."), poisonedMethodPredicate);
		}

		return null;
	}

	// inline lambdas seem to kill the JVM, wew
	static class PatchedClassPredicate implements Predicate<String>
	{
		@Override
		public boolean test(String className)
		{
			return "java/lang/String".equals(className);
		}
	}

	static class PoisonedMethodPredicate implements Predicate<CtMethod>
	{

		@Override
		public boolean test(CtMethod method)
		{
			return "substring".equals(method.getName());
		}
	}
}
