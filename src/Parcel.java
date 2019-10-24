public class Parcel {
    private int _destination;
    private int _weight;

    public Parcel(int destination, int weight) {
        _destination = destination;
        _weight = weight;
    }

    public int getDestination() {
        return _destination;
    }

    public int getWeight() {
        return _weight;
    }
}
