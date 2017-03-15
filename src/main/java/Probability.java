public interface Probability {
    float getMaxProbability();
    float getProbability(double latitude, double longitude, double velLat, double velLong);
    void setProbability(double latitude, double longitude, double velLat, double velLong, float value);
}
