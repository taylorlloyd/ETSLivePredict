import java.io.*;
import java.net.*;
import java.util.*;

import com.google.transit.realtime.*;

public class BusFeed extends Thread {
    private BusUpdateListener listener;
    private boolean running;
    private long waitTimeMs = 30*1000;
    private HashMap<String, Bus> busses = new HashMap<String, Bus>();

    private boolean live = true;
    private String replayDir = null;
    private int replayCount = 0;

    public BusFeed(BusUpdateListener listener) {
        this.listener = listener;
    }

    public BusFeed(String replayDir, BusUpdateListener listener) {
        this.live = false;
        this.replayDir = replayDir;
        this.replayCount = 0;
        this.listener = listener;
    }

    public void setListener(BusUpdateListener l) {
        this.listener = l;
        if(listener != null)
          listener.OnBussesChanged(busses);
    }

    public BusFeed startFeed() {
        this.running = true;
        this.start();
        return this;
    }

    public void stopFeed() {
        this.running = false;
    }

    public void run() {
        while(running) {
            try {
                InputStream is;
                if(live) {
                    URL feed = new URL("https://data.edmonton.ca/download/7qed-k2fc/application%2Foctet-stream");
                    URLConnection con = feed.openConnection();
                    is = con.getInputStream();
                } else {
                    if(replayCount == 120)
                        replayCount = 0;

                    File f = new File(replayDir+"/"+(replayCount++)+".gtfs");
                    is = new FileInputStream(f);
                }
                GtfsRealtime.FeedMessage msg = GtfsRealtime.FeedMessage.parseFrom(is);
                // TODO: Export busses into internal structure?
                List<GtfsRealtime.FeedEntity> entities = msg.getEntityList();

                long msgTime = msg.getHeader().getTimestamp();

                for(GtfsRealtime.FeedEntity e : entities) {
                    if(e.hasVehicle()) {
                        GtfsRealtime.VehiclePosition vp = e.getVehicle();

                        String vId = vp.getVehicle().getId();

                        Bus bus;
                        if(busses.containsKey(vId)) {
                            bus = busses.get(vId);
                        } else {
                            bus = new Bus();
                            bus.vehicleId = vId;
                            busses.put(vId, bus);
                        }

                        bus.tripId = vp.getTrip().getTripId();
                        bus.timestamp = vp.getTimestamp();
                        bus.latitude = vp.getPosition().getLatitude();
                        bus.longitude = vp.getPosition().getLongitude();
                        bus.bearing = vp.getPosition().getBearing();
                        bus.speed = vp.getPosition().getSpeed();

                        //System.out.println("Trip " + tripId + ": ( "+lat+ ", " + lng + " ) at " + timestamp);

                    }
                }
                if(listener != null)
                    listener.OnBussesChanged(busses);
                sleep(waitTimeMs);
            } catch(Exception e) {
                // Forward the issue to the listener
                if(listener != null)
                listener.OnError(e);
            }
        }
    }

    interface BusUpdateListener {
        public void OnBussesChanged(Map<String,Bus> busses);
        public void OnError(Exception e);
    }
}
