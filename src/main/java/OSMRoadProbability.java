import java.io.*;
import java.util.*;

public class OSMRoadProbability implements Probability {
    private OSMParser osm;
    private GridIndex<OSMParser.Way> index;

    private static final float ROAD_WIDTH = 0.0011f;
    private static final float ROAD_WIDTH_SQ = ROAD_WIDTH*ROAD_WIDTH;

    public OSMRoadProbability() {
        this.osm = new OSMParser();
        try {
            this.osm.parse(new FileInputStream(new File("edmmap")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Read " + this.osm.nodes.size() + " Nodes");
        System.out.println("Read " + this.osm.ways.size() + " Ways");

        this.index = new GridIndex<>();

        for(OSMParser.Way w : this.osm.ways.values()) {
            // Construct the MBR for this way
            double minLng = Double.POSITIVE_INFINITY;
            double minLat = Double.POSITIVE_INFINITY;
            double maxLng = Double.NEGATIVE_INFINITY;
            double maxLat = Double.NEGATIVE_INFINITY;

            for(OSMParser.Node n : w.path) {
                if(n.lng < minLng)
                    minLng = n.lng;
                if(n.lng > maxLng)
                    maxLng = n.lng;
                if(n.lat < minLat)
                    minLat = n.lat;
                if(n.lat > maxLat)
                    maxLat = n.lat;
            }
            index.addToGrid(minLng, minLat, maxLng, maxLat, w);
        }
    }

    @Override
    public float getMaxProbability() {
        return 0.5f;
    }

    @Override
    public LatLng getMostLikelyPoint() {
        // Just find a road
        OSMParser.Node n = this.osm.nodes.values().iterator().next();
        return new LatLng(n.lat, n.lng);
    }

    private double sqr_dist(double x1, double y1, double x2, double y2, double x3, double y3) {
        double A = x1 - x2;
        double B = y1 - y2;
        double C = x3 - x2;
        double D = y3 - y2;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = dot / len_sq;

        double xx, yy;

        if (param < 0 || (x2 == x3 && y2 == y3)) {
                xx = x2;
                yy = y2;
            }
        else if (param > 1) {
                xx = x3;
                yy = y3;
            }
        else {
                xx = x2 + param * C;
                yy = y2 + param * D;
            }

        double dx = x1 - xx;
        double dy = y1 - yy;
        return (dx*dx + dy*dy);
    }

    private double sqr_dist(double lat, double lng, OSMParser.Way line) {

        OSMParser.Node last = line.path.get(0);
        double mindist = Double.POSITIVE_INFINITY;

        for(int i=1; i<line.path.size(); ++i) {
            OSMParser.Node cur = line.path.get(i);
            double dist = sqr_dist(lat, lng, last.lat, last.lng, cur.lat, cur.lng);
            if(dist < mindist)
                mindist = dist;
            last = cur;
        }
        return mindist;
    }

    double CNDF(double x) {
        double k = (1d / ( 1d + 0.2316419 * x));
        double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
                   k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

        return 1d - y;
    }

    @Override
    public float getProbability(double latitude, double longitude, double velLat, double velLong) {
        float maxProb = 0.01f;
        for(OSMParser.Way path : this.index.intersects(longitude, latitude)) {
            float prob = (float) CNDF(Math.sqrt(sqr_dist(latitude, longitude, path)/ROAD_WIDTH_SQ));
            if(prob>maxProb)
                maxProb = prob;
        }
        return maxProb;
    }

    @Override
    public void setProbability(double latitude, double longitude, double velLat, double velLong, float value) {
        // Ignore
    }
}
