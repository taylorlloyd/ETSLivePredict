public class GPSProbability implements Probability {
    private double latitude;
    private double longitude;
    private double maxDiff = 0.001;

    public GPSProbability(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getMaxProbability() {
        return 1.0f;
    }

    public LatLng getMostLikelyPoint() {
        return new LatLng(latitude, longitude);
    }

    public float getProbability(double latitude, double longitude, double velLat, double velLong) {
        float latDiff = (float) (this.latitude - latitude);
        float lngDiff = (float) (this.longitude - longitude);

        float err = (float) Math.sqrt(latDiff*latDiff + lngDiff*lngDiff);
        return (float) CNDF(err/maxDiff);
    }

    double CNDF(double x) {
        double k = (1d / ( 1d + 0.2316419 * x));
        double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
                   k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

        return Math.max(1d - y, 0.05);
    }

    public void setProbability(double latitude, double longitude, double velLat, double velLong, float value) {
        // Ignore
    }

    public String toString() {
        return "GPSProbability(lat: " + latitude + ", long: " + longitude + ")";
    }
}
