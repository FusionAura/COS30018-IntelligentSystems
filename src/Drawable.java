public abstract interface Drawable {
    double _x = 0;
    double _y = 0;

    public abstract void Draw();
    public default double getX() {
        return _x;
    }

    public default double getY() {
        return _y;
    }
}
