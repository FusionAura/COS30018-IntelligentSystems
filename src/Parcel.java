public class Parcel {
    private String _destination;
    private int _weight;

    public Parcel(String destination, int weight) {
        _destination = destination;
        _weight = weight;
    }

    public String getDestination() {
        return _destination;
    }

    public int getWeight() {
        return _weight;
    }
}
