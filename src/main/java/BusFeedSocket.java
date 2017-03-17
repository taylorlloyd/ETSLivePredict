import java.util.*;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.*;

public class BusFeedSocket extends WebSocketAdapter implements BusTracker.BusChangeListener {
    private BusTracker tracker;
    private Session session;

    @Override
    public void onBusChanged(Bus bus, Probability p) {
        try {
            String fmt = "{\"vid\": \"" + bus.vehicleId +
                 "\", \"latitude\": " + bus.latitude +
                  ", \"longitude\": " + bus.longitude + "}";
            session.getRemote().sendString(fmt);
        } catch (IOException e) {
            this.OnError(e);
        }
    }

    public void OnError(Exception e) {
        e.printStackTrace();
        tracker.removeListener(this);
    }

    @Override
    public void onWebSocketConnect(Session sess)
    {
        super.onWebSocketConnect(sess);
        this.tracker = App.app.bustracker;
        this.session = sess;
        tracker.addListener(this);
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
        tracker.removeListener(this);
        System.out.println("Socket Closed: ["+statusCode+"] " + reason);
    }
    @Override
    public void onWebSocketError(Throwable error)
    {
        super.onWebSocketError(error);
        error.printStackTrace();
    }
}

