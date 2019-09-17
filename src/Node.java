public class Node implements Drawable {
    private String _name;

    public Node(String name) {
        _name = name;
    }

    public boolean isAddress(String address) {
        return address.equals(_name);
    }

    @Override
    public void Draw() {

    }
}
