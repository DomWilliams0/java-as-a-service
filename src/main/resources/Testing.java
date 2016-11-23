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
}
