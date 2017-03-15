import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;

public class EmbeddedServer {
    private Server server;
    public EmbeddedServer(BusTracker tracker) {
        server = new Server();
        ServerConnector conn = new ServerConnector(server);
        conn.setPort(8081);
        server.addConnector(conn);

        ServletContextHandler ctx = new ServletContextHandler();
        ctx.setContextPath("/");

        DefaultServlet defaultServlet = new DefaultServlet();
        ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
        holderPwd.setInitParameter("resourceBase", "./website");
        ctx.addServlet(holderPwd, "/*");

        ProbabilityTileServlet tileServlet = new ProbabilityTileServlet(tracker);
        ServletHolder holderTile = new ServletHolder("tiles", tileServlet);
        ctx.addServlet(holderTile, "/tile/*");

        server.setHandler(ctx);

        ServletHolder websocket = new ServletHolder("ws-events", BusFeedServlet.class);
        ctx.addServlet(websocket, "/events/*");

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("WebServer online");
    }
}
