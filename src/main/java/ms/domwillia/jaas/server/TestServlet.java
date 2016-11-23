package ms.domwillia.jaas.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestServlet extends HttpServlet
{
	private static int counter = 1;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(RidiculousServer.GSON.toJson(new State(req.getHeader("User-Agent"), counter++)));
	}

	class State
	{
		public final String name;
		public final int count;

		public State(String name, int count)
		{
			this.name = name;
			this.count = count;
		}
	}
}
