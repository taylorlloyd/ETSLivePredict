import java.util.*;

public class BusTracker implements BusFeed.BusUpdateListener {

    private int maxBusses;
    private int gridDim;

    private static final double minLat = 53.435;
    private static final double maxLat = 53.638;
    private static final double minLong = -113.674;
    private static final double maxLong = -113.338;

    public OSMRoadProbability osmRoads;

    public BusTracker(int maxBusses, int gridDim) {
        this.maxBusses = maxBusses;
        this.gridDim = gridDim;
        this.osmRoads = new OSMRoadProbability();
    }

    private ArrayList<BusChangeListener> listeners = new ArrayList<BusChangeListener>();

    public void addListener(BusChangeListener l) {
        listeners.add(l);
        System.out.println("Added update listener: " + l);
    }

    public void removeListener(BusChangeListener l) {
        listeners.remove(l);
    }

    // Bus probabilities, as of the last bus update
    private Map<Integer,GeoVelMesh> busProbability;
    private Map<Integer,Long> busTimestamp;

    public Probability getBusProbability(String vid) {
        if(busProbability.containsKey(Integer.parseInt(vid)))
            return busProbability.get(Integer.parseInt(vid));
        return null;
    }

    @Override
    public void OnBussesChanged(Map<String,Bus> busses) {
        if(busProbability == null && busses.size() > 0) {
            // TODO: Initialize. Track N busses
            busProbability = new HashMap<Integer, GeoVelMesh>();
            busTimestamp = new HashMap<Integer, Long>();
            int accepted = 0;
            for(Bus b: busses.values()) {
                if(accepted++ >= maxBusses)
                    break;

                // Construct a mesh from the GPS fix
                GPSProbability gps = new GPSProbability(b.latitude, b.longitude);
                System.out.println("Constructed GPS probability: " + gps);
                GeoVelMesh mesh = GeoVelMesh.fromProbability(gridDim, minLat, maxLat, minLong, maxLong, gps).multiply(osmRoads);
                System.out.println("Constructed Mesh: " + mesh);
                // Save the mesh and associated timestamp
                int vid = Integer.parseInt(b.vehicleId);
                busProbability.put(vid, mesh);
                busTimestamp.put(vid, b.timestamp);
                System.out.println("Timestamp: " + b.timestamp);

                System.out.println("Tracking bus " + b.vehicleId);
                LatLng loc = mesh.getMostLikelyPoint();
                b.latitude = loc.latitude;
                b.longitude = loc.longitude;
                for(BusChangeListener l : listeners) {
                    l.onBusChanged(b, mesh);
                }
            }
            return;
        }
        int updated = 0;
        for (Bus b: busses.values()) {
            int vid = Integer.parseInt(b.vehicleId);
            if(busProbability.containsKey(vid)) {
                updated++;
                GeoVelMesh prob = busProbability.get(vid);

                // Propagate the probability forward to the next input
                // then multiply in the new GPS fix
                long timestamp = busTimestamp.get(vid);
                long diff = b.timestamp - timestamp;
                if(diff > 0) {
                    //prob = prob.propagateTime(diff*1000)
                    //    .multiply(new GPSProbability(b.latitude, b.longitude))
                    prob = GeoVelMesh.fromProbability(gridDim, minLat, maxLat,  minLong, maxLong,
                               new GPSProbability(b.latitude, b.longitude))
                               .multiply(osmRoads);

                    busProbability.put(vid, prob);
                    busTimestamp.put(vid, b.timestamp);
                    System.out.println("[Bus "+b.vehicleId+"] Updated Mesh: " + prob);
                    LatLng point = prob.getMostLikelyPoint();
                    b.latitude = point.latitude;
                    b.longitude = point.longitude;
                }
                System.out.println("Timestamp: " + b.timestamp);
                for(BusChangeListener l : listeners) {
                    l.onBusChanged(b, prob);
                }
            }
            // TODO: Update the bus location based on the mesh
        }
        System.out.println("Updated " + updated + " busses (" + (busses.size() - updated) + " ignored)");
    }
    @Override
    public void OnError(Exception e) {
        e.printStackTrace();
    }
    interface BusChangeListener {
        public void onBusChanged(Bus b, Probability p);
    }
}
