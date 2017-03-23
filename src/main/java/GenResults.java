public class GenResults {

    public static void main(String[] args) {
        if(args.length < 1)
            System.exit(2);
        BusFeed busfeed = new BusFeed("data_capture/Mar22", 50, null);
        BusTracker bustracker = new BusTracker(1000); // All of them
        busfeed.setListener(bustracker);
        bustracker.addListener(new BusCsvWriter(args[0]));
        busfeed.startFeed();
    }
}
