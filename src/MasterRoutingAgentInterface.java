public interface MasterRoutingAgentInterface {
    void startRouting();
    void newNode(Node newNode);
    void removeNode(Node node);
    void addParcel(Parcel p);
    void removeParcel(Parcel p);
}
