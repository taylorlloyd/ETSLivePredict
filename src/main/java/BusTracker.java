import java.util.*;

public class BusTracker implements BusFeed.BusUpdateListener {

    private static final int maxBusses = 10;

    private static final double minLat = 53.478823;
    private static final double maxLat = 53.558637;
    private static final double minLong = -113.535547;
    private static final double maxLong = -113.431864;

    private BusFeed.BusUpdateListener listener;

    // Bus probabilities, as of the last bus update
    private Map<String,GeoVelMesh> busProbability;
    private Map<String,Long> busTimestamp;

    @Override
    public void OnBussesChanged(Map<String,Bus> busses) {
        if(busProbability == null && busses.size() > 0) {
            // TODO: Initialize. Track N busses
            busProbability = new HashMap<String, GeoVelMesh>();
            busTimestamp = new HashMap<String, Long>();
            int accepted = 0;
            for(Bus b: busses.values()) {
                if(accepted++ >= maxBusses)
                    break;

                // Construct a mesh from the GPS fix
                GPSProbability gps = new GPSProbability(b.latitude, b.longitude);
                System.out.println("Constructed GPS probability: " + gps);
                GeoVelMesh mesh = GeoVelMesh.fromProbability(minLat, maxLat, minLong, maxLong, gps);
                System.out.println("Constructed Mesh: " + mesh);
                // Save the mesh and associated timestamp
                busProbability.put(b.vehicleId, mesh);
                busTimestamp.put(b.vehicleId, b.timestamp);
            }
            return;
        }
        for (Bus b: busses.values()) {
            if(busProbability.containsKey(b.vehicleId)) {
                GeoVelMesh prob = busProbability.get(b.vehicleId);

                // Propagate the probability forward to the next input
                // then multiply in the new GPS fix
                long timestamp = busTimestamp.get(b.vehicleId);
                long diff = b.timestamp - timestamp;
                if(diff <= 0)
                    continue;

                prob = prob.propagateTime(diff)
                           .multiply(new GPSProbability(b.latitude, b.longitude));

                busProbability.put(b.vehicleId, prob);
                busTimestamp.put(b.vehicleId, timestamp);
            }
        }

    }
    @Override
    public void OnError(Exception e) {
        e.printStackTrace();
    }
}
