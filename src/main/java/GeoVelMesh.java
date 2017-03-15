import java.util.*;

public class GeoVelMesh extends ProbabilityMesh implements Probability {

    private static final double maxVel = 0.299; // degrees of latitude/second (~120 km/h)
    private static final double minVel = (-1)*maxVel; // Velocities can be in either direction
    private double minLat;
    private double maxLat;
    private double minLong;
    private double maxLong;

    private static final int spaceDimSize = 100;
    private static final int velDimSize = 9;

    public GeoVelMesh(double minLat, double maxLat, double minLong, double maxLong) {
        super(new int[] {spaceDimSize, spaceDimSize, velDimSize, velDimSize});

        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLong = minLong;
        this.maxLong = maxLong;
    }

    public GeoVelMesh clone() {
        GeoVelMesh newMesh = new GeoVelMesh(minLat, maxLat, minLong, maxLong);
        int[] idx = new int[4];
        for(int i=0; i<spaceDimSize; i++) {
            idx[0] = i;
            for(int j=0; j<spaceDimSize; j++) {
                idx[1] = j;
                for(int k=0; k<velDimSize; k++) {
                    idx[2] = k;
                    for(int l=0; l<velDimSize; l++) {
                        idx[3] = l;
                        newMesh.setPoint(idx, getPoint(idx));
                    }
                }
            }
        }
        return newMesh;
    }
    public static GeoVelMesh fromProbability(double minLat, double maxLat, double minLong, double maxLong, Probability p) {
        GeoVelMesh mesh = new GeoVelMesh(minLat, maxLat, minLong, maxLong);
        int[] idx = new int[4];
        for(int i=0; i<spaceDimSize; i++) {
            idx[0] = i;
            for(int j=0; j<spaceDimSize; j++) {
                idx[1] = j;
                for(int k=0; k<velDimSize; k++) {
                    idx[2] = k;
                    for(int l=0; l<velDimSize; l++) {
                        idx[3] = l;
                        float prob = p.getProbability(
                            denormalize(i,minLat,maxLat,spaceDimSize),
                            denormalize(j,minLong,maxLong,spaceDimSize),
                            denormalize(k,minVel,maxVel,velDimSize),
                            denormalize(l,minVel,maxVel,velDimSize)
                        );
                        mesh.setPoint(idx, prob);
                    }
                }
            }
        }
        return mesh;
    }

    public GeoVelMesh multiply(Probability p) {
        GeoVelMesh newMesh = new GeoVelMesh(minLat, maxLat, minLong, maxLong);
        int[] idx = new int[4];
        for(int i=0; i<spaceDimSize; i++) {
            idx[0] = i;
            double latitude = denormalize(i, minLat, maxLat, spaceDimSize);
            for(int j=0; j<spaceDimSize; j++) {
                idx[1] = j;
                double longitude = denormalize(j,minLong,maxLong,spaceDimSize);
                for(int k=0; k<velDimSize; k++) {
                    idx[2] = k;
                    double velLat = denormalize(k, minVel, maxVel, velDimSize);
                    for(int l=0; l<velDimSize; l++) {
                        idx[3] = l;
                        double velLong = denormalize(l, minVel, maxVel, velDimSize);
                        float mine = getPoint(idx);
                        float theirs = getProbability(latitude, longitude, velLat, velLong);
                        newMesh.setPoint(idx, mine*theirs);
                    }
                }
            }
        }
        return newMesh;
    }

    public GeoVelMesh propagateTime(long ms) {
        GeoVelMesh newMesh = new GeoVelMesh(minLat, maxLat, minLong, maxLong);
        int[] idx = new int[4];
        for(int i=0; i<spaceDimSize; i++) {
            idx[0] = i;
            double latitude = denormalize(i, minLat, maxLat, spaceDimSize);
            for(int j=0; j<spaceDimSize; j++) {
                idx[1] = j;
                double longitude = denormalize(j,minLong,maxLong,spaceDimSize);
                for(int k=0; k<velDimSize; k++) {
                    idx[2] = k;
                    double velLat = denormalize(k, minVel, maxVel, velDimSize);
                    for(int l=0; l<velDimSize; l++) {
                        idx[3] = l;
                        double velLong = denormalize(l, minVel, maxVel, velDimSize);

                        float p = getPoint(idx);
                        double newlat = latitude + velLat*(double)(ms/1000);
                        double newlong = longitude + velLong*(double)(ms/1000);
                        newMesh.setProbability(newlat, newlong, velLat, velLong, p);
                    }
                }
            }
        }
        return newMesh;
    }

    public float getProbability(double lat, double lng, double velLat, double velLng) {
        List<WeightedPoint> points = toGridIndices(lat, lng, velLat, velLng);
        double sum = 0;
        for(WeightedPoint p : points)
            sum += p.weight;

        float value = 0.0f;
        for(WeightedPoint p : points)
            value += getPoint(p.idx) * (p.weight/sum);
        return value;
    }

    public void setProbability(double lat, double lng, double velLat, double velLng, float val) {
        List<WeightedPoint> points = toGridIndices(lat, lng, velLat, velLng);
        double sum = 0;
        for(WeightedPoint p : points)
            sum += p.weight;
        // Accept only a percentage of the change
        for(WeightedPoint p : points)
            setPoint(p.idx, (float) ((val - getPoint(p.idx)) * (p.weight/sum)));
    }


    private List<WeightedPoint> toGridIndices(double lat, double lng, double velLat, double velLng) {
        // Find the corresponding point in the grid
        float[] idx = new float[] { normalize(lat, minLat, maxLat, spaceDimSize),
                                    normalize(lng, minLong, maxLong, spaceDimSize),
                                    normalize(velLat, minVel, maxVel, velDimSize),
                                    normalize(velLng, minVel, maxVel, velDimSize) };

        // Collect the points, weighted by distance
        List<WeightedPoint> points = new ArrayList<WeightedPoint>();
        collectIndicesRecurse(idx, new int[0], points);
        return points;
    }

    private void collectIndicesRecurse(float[] idx, int[] path, List<WeightedPoint> points) {
        if(path.length == idx.length) {
            points.add(new WeightedPoint(distance(idx, path), path));
            return;
        }

        int level = path.length;
        float loc = idx[path.length];
        float min = (float) Math.floor(idx[path.length]);
        float max = (float) Math.ceil(idx[path.length]);

        // No need to select point from both sides
        if(loc == min || loc == max) {
            int[] npath = new int[path.length+1];
            for(int i=0; i<path.length; i++)
                npath[i] = path[i];
            npath[path.length] = (int) loc;
            collectIndicesRecurse(idx, npath, points);
        } else {
            int[] minpath = new int[path.length+1];
            int[] maxpath = new int[path.length+1];
            for(int i=0; i<path.length; i++) {
                minpath[i] = path[i];
                maxpath[i] = path[i];
            }
            minpath[path.length] = (int) min;
            maxpath[path.length] = (int) max;
            collectIndicesRecurse(idx, minpath, points);
            collectIndicesRecurse(idx, maxpath, points);
        }
    }

    private static float normalize(double val, double min, double max, int dimSize) {
        if(val<min)
            return 0.0f;
        if(val>max)
            return (float) dimSize;
        return (float) ((val-min)*dimSize/(max-min));
    }

    public float getMaxProbability() {
        int[] maxidx = mostLikelyPoint();
        return getPoint(maxidx);
    }

    private int[] mostLikelyPoint() {
        int[] maxIdx = new int[4];
        float maxVal = -1000000;
        int[] idx = new int[4];
        for(int i=0; i<spaceDimSize; i++) {
            idx[0] = i;
            for(int j=0; j<spaceDimSize; j++) {
                idx[1] = j;
                for(int k=0; k<velDimSize; k++) {
                    idx[2] = k;
                    for(int l=0; l<velDimSize; l++) {
                        idx[3] = l;
                        if(getPoint(idx)>maxVal) {
                            maxIdx[0] = idx[0];
                            maxIdx[1] = idx[1];
                            maxIdx[2] = idx[2];
                            maxIdx[3] = idx[3];
                            maxVal = getPoint(idx);
                        }
                    }
                }
            }
        }
        return maxIdx;
    }

    public String toString() {
        int[] mlp = mostLikelyPoint();
        double latitude = denormalize(mlp[0], minLat, maxLat, spaceDimSize);
        double longitude = denormalize(mlp[1], minLong, maxLong, spaceDimSize);

        return "GeoVelMesh(lat: " + latitude + ", long: " + longitude + ")";
    }

    private static double denormalize(float val, double min, double max, int dimSize) {
        return min + (val*(max-min)/dimSize);
    }

    private static float distance(float[] p1, int[] p2) {
        float sq_dist = 0;
        for(int i=0; i<p1.length; i++) {
            float dist = p1[i] - p2[i];
            sq_dist += dist*dist;
        }
        return sq_dist;
    }

    private static class WeightedPoint {
        public float weight;
        public int[] idx;
        public WeightedPoint(float weight, int[] idx) {
            this.weight = weight;
            this.idx = idx;
        }
    }
}
