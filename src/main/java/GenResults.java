public class GenResults {

    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("GenResults outputfolder randomness gridsize");
            System.exit(2);
        }
        double randomness = Double.parseDouble(args[1]);
        int gridDim = Integer.parseInt(args[2]);
        BusFeed busfeed = new BusFeed("data_capture/Mar22", 50, randomness, null);
        BusTracker bustracker = new BusTracker(5, gridDim); // First 5 busses
        busfeed.setListener(bustracker);
        bustracker.addListener(new BusCsvWriter(args[0]));
        busfeed.startFeed();
    }
}
