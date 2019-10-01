import java.io.Serializable;

public class Position implements Serializable {
    private double _x;
    private double _y;

    public Position(double x, double y) {
        _x = x;
        _y = y;
    }

    public double getX() {
        return _x;
    }

    public void setX(double x) {
        _x = x;
    }

    public double getY() {
        return _y;
    }

    public void setY(double y) {
        _y = y;
    }
}
