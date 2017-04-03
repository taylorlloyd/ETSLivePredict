import java.util.Random;

public class GPSFactory {
    double maxRand;
    boolean addRand;

    public GPSFactory() {
        this.addRand = false;
    }

    public GPSFactory(double maxRand) {
        this.addRand = true;
        this.maxRand = maxRand;
    }

    public GPSProbability get(double latitude, double longitude) {
        if(addRand) {
            Random r = new Random();
            // + or - maxRand
            latitude += 2*r.nextDouble()*maxRand - maxRand;
            longitude += 2*r.nextDouble()*maxRand - maxRand;
        }
        return new GPSProbability(latitude, longitude);
    }
}
