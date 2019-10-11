import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;

public class Node implements Drawable, Serializable {
    private String _name;
    private Circle _body;

    public Node(String name, Position position) {
        _name = name;
        _body = new Circle(position.getX(), position.getY(), 10, Color.GRAY);
    }
    public boolean isAddress(String address) {
        return address.equals(_name);
    }

    @Override
    public void Draw() {

    }

    public Circle getBody() {
        return _body;
    }

    public double getX() {
        return _body.getCenterX();
    }

    public double getY() {
        return _body.getCenterY();
    }
}
