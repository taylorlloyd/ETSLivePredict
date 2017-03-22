import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.io.*;

public class OSMParser extends DefaultHandler {
    Map<Long, Node> nodes;
    Map<Long, Way> ways;

    private Way current;

    public OSMParser() {
        super();
        this.nodes = new HashMap<>();
        this.ways = new HashMap<>();
    }

    public Way getWay(int id) {
        Way match = ways.get(id);
        if (match != null)
            return match;
        // We lost bits to store the way, search for it
        for(Way w : ways.values()) {
            int trunc_id = (int) w.id;
            if(trunc_id == id)
                return w;
        }

        return null;
    }

    public void parse(InputStream input) throws SAXException, IOException {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setContentHandler(this);
        xr.setErrorHandler(this);
        xr.parse(new InputSource(input));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        super.startElement(uri, localName, qName, attr);
        if(localName.equals("node")) {
            Node n = new Node(
                Long.parseLong(attr.getValue("id")),
                Double.parseDouble(attr.getValue("lat")),
                Double.parseDouble(attr.getValue("lon"))
            );
            nodes.put(n.id, n);
        } else if(localName.equals("way")) {
            Way w = new Way(Long.parseLong(attr.getValue("id")), new ArrayList<>());
            current = w;
        } else if(localName.equals("nd")) {
            if(current != null) {
                current.path.add(nodes.get(Long.parseLong(attr.getValue("ref"))));
            }
        } else if(localName.equals("tag")) {
            if(current != null) {
                if(attr.getValue("k").equals("highway")) {
                    // This is a road
                    ways.put(current.id, current);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if(localName.equals("way"))
            current = null;
    }

    static class Node {
        long id;
        double lat;
        double lng;
        public Node(long id, double lat, double lng) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
        }
    }

    static class Way {
        long id;
        List<Node> path;
        public Way(long id, List<Node> path) {
            this.id = id;
            this.path = path;
        }
    }
}
