import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;

public class Node implements Serializable {
    private String _name;
    private Position _position;

    public Node(String name, Position position) {
        _name = name;
        _position = position;
    }

    public String getName() {
        return _name;
    }

    public boolean amI(String name) {
        return name.equals(_name);
    }

    //  Circles aren't serializable so we just create the body here
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
