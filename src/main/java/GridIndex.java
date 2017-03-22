import java.util.*;

public class GridIndex<T> {

    private Map<Integer, Map<Integer, List<T>>> grid;
    private int maxPerCell = 1;

    public GridIndex() {
        grid = new HashMap<>();
    }

    private int toBucket(double val) {
        return (int) (val*4000);
    }

    private List<T> getCellList(int x, int y) {
        Map<Integer, List<T>> gridY;
        if(grid.containsKey(x)) {
            gridY = grid.get(x);
        } else {
            gridY = new HashMap<>();
            grid.put(x, gridY);
        }
        List<T> cell;
        if(gridY.containsKey(y)) {
            cell = gridY.get(y);
        } else {
            cell = new ArrayList<>();
            gridY.put(y, cell);
        }
        return cell;
    }

    public List<T> intersects(double x, double y) {
        // Collect all adjacent buckets as well
        List<T> elems = new ArrayList<>();
        int xb = toBucket(x);
        int yb = toBucket(y);

        elems.addAll(getCellList(xb-1, yb-1));
        elems.addAll(getCellList(xb-1, yb));
        elems.addAll(getCellList(xb-1, yb+1));

        elems.addAll(getCellList(xb, yb-1));
        elems.addAll(getCellList(xb, yb));
        elems.addAll(getCellList(xb, yb+1));

        elems.addAll(getCellList(xb+1, yb-1));
        elems.addAll(getCellList(xb+1, yb));
        elems.addAll(getCellList(xb+1, yb+1));

        return elems;
    }

    public void addToGrid(double minX, double minY, double maxX, double maxY, T elem) {
        int minBucketX = toBucket(minX);
        int maxBucketX = toBucket(maxX);
        int minBucketY = toBucket(minY);
        int maxBucketY = toBucket(maxY);
        for(int x = minBucketX; x<maxBucketX; ++x) {
            for(int y = minBucketY; y<maxBucketY; ++y) {
                List<T> l = getCellList(x, y);
                l.add(elem);
                if(l.size() > maxPerCell) {
                    maxPerCell = l.size();
                    System.out.println("[GridIndex] Largest cell size: " + maxPerCell);
                }
            }
        }
    }
}
