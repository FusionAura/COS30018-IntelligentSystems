import java.io.Serializable;

public class Node implements Drawable, Serializable {
    private String _name;
    private Position _position = new Position(0, 0);

    public Node(String name) {
        _name = name;
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
