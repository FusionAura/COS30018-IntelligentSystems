import java.util.List;

public interface MasterRoutingAgentInterface {
    void startRouting(List<String> deliveryAgents);
    void newNode(Node newNode);
    void removeNode(Node node);
    void addParcel(Parcel p);
    void removeParcel(Parcel p);
}
