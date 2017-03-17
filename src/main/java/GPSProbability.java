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

        float err = (latDiff*latDiff + lngDiff*lngDiff);
        float boundary = (float)(maxDiff*maxDiff);
        return boundary / (boundary + err);
    }

    public void setProbability(double latitude, double longitude, double velLat, double velLong, float value) {
        // Ignore
    }

    public String toString() {
        return "GPSProbability(lat: " + latitude + ", long: " + longitude + ")";
    }
}
