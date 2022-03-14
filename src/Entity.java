public class Entity {
    private String label;
    private int[] vector;

    public Entity(String label, int[] vector) {
        this.label = label;
        this.vector = vector;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int[] getVector() {
        return vector;
    }

    public void setVector(int[] vector) {
        this.vector = vector;
    }
}
