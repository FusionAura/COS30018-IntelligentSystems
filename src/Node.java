import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;

public class Node implements Drawable, Serializable {
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

    @Override
    public void GetAgent() {
    }
  
    public Circle getBody() {
        Circle body = new Circle(_position.getX(), _position.getY(), 10, Color.GRAY);
        body.toBack();
        return body;
    }


    public double getX() {
        return _position.getX();
    }

    public double getY() {
        return _position.getY();
    }
}
