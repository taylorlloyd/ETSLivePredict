import java.io.*;
public class BusCsvWriter implements BusTracker.BusChangeListener {
    String dir;

    public BusCsvWriter(String dir) {
        this.dir = dir;
    }

    private File fileForBus(String vid) throws IOException {
        File dirFile = new File(dir);
        if(!dirFile.exists())
            dirFile.mkdirs();
        return new File(dir+"/"+vid+".csv");
    }

    @Override
    public void onBusChanged(Bus bus, Probability p) {
        try {
            FileWriter fw = new FileWriter(fileForBus(bus.vehicleId), true);
            LatLng ll = p.getMostLikelyPoint();
            fw.write(bus.timestamp + ", " + ll.latitude + ", " + ll.longitude + "\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            this.OnError(e);
        }
    }

    public void OnError(Exception e) {
        e.printStackTrace();
    }
}
