package ms.domwillia.jaas.server;

import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorHandler extends org.eclipse.jetty.server.handler.ErrorHandler
{
	@Override
	protected void generateAcceptableResponse(Request baseRequest, HttpServletRequest request, HttpServletResponse response, int code, String message, String mimeType) throws IOException
	{
		response.setContentType("application/json");
		response.setStatus(code);
		response.getWriter().write(RidiculousServer.GSON.toJson(new Error(code)));

		baseRequest.setHandled(true);
	}

	class Error
	{
		public final int error;

		public Error(int error)
		{
			this.error = error;
		}
	}
}
