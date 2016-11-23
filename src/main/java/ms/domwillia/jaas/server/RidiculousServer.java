package ms.domwillia.jaas.server;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class RidiculousServer
{
	public static final Gson GSON = new Gson();

	public static void main(String[] args)
	{
		int port = 65040;
		String host = "127.0.0.1";
		Server server = new Server();

		// connector
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		connector.setHost(host);

		// handlers
		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		servletContext.setErrorHandler(new ErrorHandler());
		servletContext.addServlet(TestServlet.class, "/test");

		try
		{
			server.setHandler(servletContext);
			server.addConnector(connector);

			server.start();
			server.dumpStdErr();
			server.join();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
