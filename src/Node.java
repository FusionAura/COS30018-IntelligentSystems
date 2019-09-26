public class Node implements Drawable {
    private String _name;
    private Position _position;

    public Node(String name, Position position) {
        _name = name;
        _position = position;
    }

    public boolean isAddress(String address) {
        return address.equals(_name);
    }

    @Override
    public void Draw() {

    }

    public double getX() {
        return _position.getX();
    }

    public double getY() {
        return _position.getY();
    }
}
