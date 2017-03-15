import java.util.*;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.*;

public class BusFeedSocket extends WebSocketAdapter implements BusFeed.BusUpdateListener {
    private Session session;

    public void OnBussesChanged(Map<String,Bus> busses) {
        if(session == null)
            return;

        System.out.println("Updated "+busses.size()+ " busses.");
        try {
            for(Bus bus : busses.values()) {
                String fmt = "{\"vid\": \"" + bus.vehicleId +
                     "\", \"latitude\": " + bus.latitude +
                      ", \"longitude\": " + bus.longitude + "}";
                session.getRemote().sendString(fmt);
            }
        } catch (IOException e) {
            this.OnError(e);
        }
    }

    public void OnError(Exception e) {
        e.printStackTrace();
        session = null;
    }

    @Override
    public void onWebSocketConnect(Session sess)
    {
        super.onWebSocketConnect(sess);
        this.session = sess;
        App.app.busfeed.setListener(this);
        System.out.println("Socket connected");
    }

    @Override
    public void onWebSocketText(String msg)
    {
        super.onWebSocketText(msg);
        System.out.println("Recieved TEXT message: " + msg);
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        System.out.println("Socket Closed: ["+statusCode+"] " + reason);
    }
    @Override
    public void onWebSocketError(Throwable error)
    {
        super.onWebSocketError(error);
        error.printStackTrace();
    }
}

