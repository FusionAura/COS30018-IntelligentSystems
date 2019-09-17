public class Node {
    private String _name;

    public Node(String name) {
        _name = name;
    }

    public boolean isAddress(String address) {
        return address.equals(_name);
    }
}
