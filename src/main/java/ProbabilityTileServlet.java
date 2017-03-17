import javax.servlet.http.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

class ProbabilityTileServlet extends HttpServlet {

    private BusTracker tracker;

    public ProbabilityTileServlet(BusTracker tracker) {
        this.tracker = tracker;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            double minLat = Double.parseDouble(req.getParameter("minlat"));
            double maxLat = Double.parseDouble(req.getParameter("maxlat"));
            double minLong = Double.parseDouble(req.getParameter("minlong"));
            double maxLong = Double.parseDouble(req.getParameter("maxlong"));

            String vid = req.getParameter("vid");

            resp.setHeader("Content-Type", "image/png");
            // resp.setHeader("Content-Length", imageBytes.length);

            // TODO: get the probability object
            Probability p = tracker.getBusProbability(vid);
            //Probability gps = new GPSProbability(53.50, -113.45);
            //GeoVelMesh p = GeoVelMesh.fromProbability(minLat, maxLat, minLong, maxLong, gps);


            // Write the probability to the alpha layer
            BufferedImage img = new BufferedImage(256,256, BufferedImage.TYPE_INT_ARGB);
            float maxVal = p.getMaxProbability();
            System.out.println("Maximum probability: " + maxVal);
            for(int x=0; x<256; ++x) {
                double longitude = minLong + x*((maxLong-minLong)/256);
                for(int y=0; y<256; ++y) {
                    double latitude = maxLat - y*((maxLat-minLat)/256);
                    // TODO: Use color to represent velocity
                    float prob = p.getProbability(latitude, longitude, 0.0, 0.0);
                    int rgb = 0x00009933; // Teal color
                    int alpha = (int) (300*prob/maxVal);
                    if(alpha > 255)
                        alpha = 255;
                    img.setRGB(x, y, (alpha << 24) | rgb);
                }
            }

            ImageIO.write( img, "png", resp.getOutputStream() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
