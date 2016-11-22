public class DummyMain
{
	public static void main(String[] args)
	{
		String s = "Chris Lane"; // kek
		if (s.charAt(0) == 'C')
		{
			s = s.substring(6) + s.substring(5, 6) + s.substring(0, 5);
		}

		System.out.println("s = " + s);
	}
}
