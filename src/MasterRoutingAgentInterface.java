public interface MasterRoutingAgentInterface {
    void StartRouting();
    void NewNode(Node newNode);
    void RemoveNode(Node node);
    void AddParcel(Parcel p);
}
