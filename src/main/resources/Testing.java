package ms.domwillia.jaas.patching.code;

public class Testing
{
	void testCode()
	{
		// #BEGIN test

		System.out.println("this is some test code!");
		for (int i = 0; i < 5; i++)
		{
			// cheeky comment
			// good comment #BEGIN
			System.out.println("i = " + i);
		}

		// #END

		// never reached
		throw new IllegalStateException();
	}

	void printArgs()
	{
		// #BEGIN printObject
		System.out.print(o.toString() + ":" + o.getClass().getName());
		// #END

		// #BEGIN pre
		System.out.print(">>> #$methodName(");

		Object[] args = $args;
		Object o = args.length > 0 ? args[0] : null;
		if (o != null)
		{
			#(printObject)#
		}

		for (int i = 1; i < args.length; i++)
		{
			Object o = args[i];
			System.out.print(", ");
			#(printObject)#
		}

		System.out.println(")");
		// #END
	}
}
