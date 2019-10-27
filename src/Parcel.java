public class Parcel {
    private String _destination;
    private int _weight;
    private String _description;

    public Parcel(int weight, String destination, String description) {
        _destination = destination;
        _weight = weight;
        _description = description;
    }

    public String getDestination() {
        return _destination;
    }

    public int getWeight() {
        return _weight;
    }

    public String getDescription() {
        return _description;
    }

    @Override
    public String toString() {
        return _description.toUpperCase() + "\nWeight: " +_weight + "\nDestination: " + _destination;
    }
}
