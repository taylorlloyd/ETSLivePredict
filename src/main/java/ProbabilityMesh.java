public class ProbabilityMesh {
    private float[] mesh;
    private int[] dims;

    public ProbabilityMesh(int[] dims) {
        int elemSize = 1;
        for(int d : dims) {
            elemSize *= d;
        }
        this.mesh = new float[elemSize];
        this.dims = dims;
    }

    float getPoint(int[] indices) {
        int index = 0;
        int elemSize = 1;
        for(int i=dims.length-1; i>=0; i--) {
            if(indices[i] < 0)
                indices[i] = 0;
            else if(indices[i]>=dims[i])
                indices[i] = dims[i] - 1;
            index += indices[i]*elemSize;
            elemSize *= dims[i];
        }
        return mesh[index];
    }

    void setPoint(int[] indices, float val) {
        int index = 0;
        int elemSize = 1;
        for(int i=dims.length-1; i>=0; i--) {
            index += indices[i]*elemSize;
            elemSize *= dims[i];
        }
        mesh[index] = val;
    }
}
