import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class MasterRoutingAgent extends Agent implements MasterRoutingAgentInterface {
    public static final String DELIVERY_ROUTE_ONTOLOGY = "delivery-route";
    public static String GET_CAPACITY_REQUEST_ONTOLOGY = "capacity-request";
    public static String GET_CAPACITY_RESPONSE_ONTOLOGY = "capacity-response";

    private List<Integer> _vehicleCapacity = new ArrayList<>();
    private Position _position = new Position(100, 100);
    private List<List<Double>> _distanceMatrix = new ArrayList<List<Double>>();
    private List<Node> _allNodes = new ArrayList<Node>();
    private Integer _responses = 0;
    private List<Parcel> _allParcel = new ArrayList<Parcel>();

    protected void setup() {
        //https://stackoverflow.com/questions/28652869/how-to-get-agents-on-all-containers-jade
        registerO2AInterface(MasterRoutingAgentInterface.class, this);

        // Setting up message listener so that it can recieve messages from other agents
        CyclicBehaviour msgListenBehaviour = new CyclicBehaviour(this) {

            public void action() {
                ACLMessage msg = receive();
                if(msg!=null) {
                    if (msg.getOntology().equals(GET_CAPACITY_RESPONSE_ONTOLOGY)) {
                        // Make sure to do each message one at a time
                        synchronized (MasterRoutingAgent.class) {
                            _responses++;
                            // id,capacity
                            String[] response = msg.getContent().split(",");
                            _vehicleCapacity.set(Integer.parseInt(response[0]), Integer.valueOf(response[1]));
                        }
                    }
                }
                //block();
            }
        };
        addBehaviour(msgListenBehaviour);

        //Register this as a node
        newNode(new Node("warehouse", _position));
    }

    private List<AID> getDeliveryAgents() {
        AMSAgentDescription[] agents;
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (-1L);
            agents = AMSService.search( this, new AMSAgentDescription(), c );
        } catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
            agents = new AMSAgentDescription[]{};
        }
        List<AID> deliveryAgents = new ArrayList<>();

        AID myID = getAID(); //This method to get the identity of //agents such as (Name , address , host ....etc)
        for (AMSAgentDescription agent : agents) {
            AID agentID = agent.getName();
            //if (!agentID.equals(myID) && !agentID.equals("ams@10.0.0.132:8888/JADE") && !agentID.equals("MasterRoutingAgent@10.0.0.132:8888/JADE")&& !agentID.equals("df@10.0.0.132:8888/JADE"))
            //{
            if (agentID.getName().matches("^d\\d+@.*$"))
            {
                deliveryAgents.add(agentID);
                System.out.println(deliveryAgents.size());
            }
        }

        return deliveryAgents;
    }

    @Override
    public void startRouting() {
        try {
            sendRoutes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method first sets up the data model
    // Then it creates routes based on the routing algorithm
    // Finally, it sends each route to their respective agent
    private void sendRoutes() throws IOException {
        // Setup data model
        List<AID> deliveryAgents = getDeliveryAgents();
        RoutingV2 VRPRoute = new RoutingV2();
        getCapacity(deliveryAgents);

        List<Integer> demands = new ArrayList<>();
        _distanceMatrix.forEach(doubles -> demands.add(0));

        List<Integer> parcelWeight = new ArrayList<>();
        _distanceMatrix.forEach(distance -> parcelWeight.add(0));
        for (Parcel parcel : _allParcel) {
            Optional<Node> destination = _allNodes.stream()
                    .filter(node -> node.amI(parcel.getDestination()))
                    .findFirst();
            if (destination.isPresent()) {
                int index = _allNodes.indexOf(destination.get());
                demands.set(index, 1);
                parcelWeight.set(index, parcel.getWeight());
            }
        }

        DataModel dataModel = new DataModel(
                _distanceMatrix,
                deliveryAgents.size(),
                demands,
                _vehicleCapacity,
                0,
                parcelWeight
        );

        //Create the routes
        List<List<Integer>> routes = VRPRoute.vehicleRouting(dataModel);

        for (int i = 0; i < deliveryAgents.size(); i++) {
            List<Node> route = new ArrayList<Node>();

            AID agent = deliveryAgents.get(i);

            // Convert the indexs into nodes that we can follow
            for (int nodePos : routes.get(i)) {
                route.add(_allNodes.get(nodePos));
            }

            MessageObject msgObject = new MessageObject();
            msgObject.setRoute(route);

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setLanguage("English");

            msg.setOntology(DELIVERY_ROUTE_ONTOLOGY);

            msg.setContentObject(msgObject);
            msg.addReceiver(agent);

            send(msg);
        }
    }

    public void addParcel(Parcel p) {
        _allParcel.add(p);
    }
    public void removeParcel(Parcel p) {
        _allParcel.remove(p);
    }
    @Override
    // Notify that a new node has been created and update the distance matrix
    public synchronized void newNode(Node newNode) {
        if (_allNodes.contains(newNode)) {
            return;
        }

        List<Double> distanceBetweenNodes = new ArrayList<>();
        for (int i = 0; i < _allNodes.size(); i++) {
            // Calculate distance using trigonometry
            double xDifference = newNode.getX() - _allNodes.get(i).getX();
            double yDifference = newNode.getY() - _allNodes.get(i).getY();

            // a^2 + b^2 = c^2
            Double distance = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));

            distanceBetweenNodes.add(distance);
            _distanceMatrix.get(i).add(distance);
        }

        // Add the distance to itself (used in matrix)
        distanceBetweenNodes.add((double) 0);

        _distanceMatrix.add(distanceBetweenNodes);
        _allNodes.add(newNode);
    }

    public synchronized void removeNode(Node node) {
        int position = _allNodes.indexOf(node);
        if (position >= 0) {
            removeNode(position);
        }
    }

    // Notify that a node has been removed and update the distance matrix
    public void removeNode(int position) {
        // Remove all distances for the node in the given position
        for (int i = 0; i < _distanceMatrix.size(); i++) {
            if (i == position) {
                continue;
            }

            _distanceMatrix.get(i).remove(position);
        }

        _distanceMatrix.remove(position);
        _allNodes.remove(position);
    }

    // Sends a request to each given agent and waits until all agents respond
    // This assumes that all agents will respond
    private void getCapacity(List<AID> deliveryAgents) {
        _vehicleCapacity.clear();
        for (int i = 0; i < deliveryAgents.size(); i++) {
            _vehicleCapacity.add(0);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setLanguage("English");
            msg.setOntology(GET_CAPACITY_REQUEST_ONTOLOGY);
            msg.addReceiver(deliveryAgents.get(i));
            msg.setContent(String.valueOf(i));
            send(msg);
        }

        while (_responses != deliveryAgents.size()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public List<List<Double>> getDistanceMatrix() {
        return _distanceMatrix;
    }
    
    //MEANT ONLY FOR TESTING DISTANCE MATRIX
    public static void main(String[] args) {
        MasterRoutingAgent masterRoutingAgent = new MasterRoutingAgent();
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            Node newNode = new Node("Node" + i, new Position(rand.nextInt(100), rand.nextInt(100)));
            masterRoutingAgent.newNode(newNode);
        }

        DecimalFormat myFormat = new DecimalFormat("##");

        for (int i = 0; i < masterRoutingAgent.getDistanceMatrix().size(); i++) {
            System.out.print("[");
            for (int j = 0; j < masterRoutingAgent.getDistanceMatrix().get(i).size(); j++) {
                System.out.print(myFormat.format(masterRoutingAgent.getDistanceMatrix().get(i).get(j)) + ", ");
            }
            System.out.println("]");
        }
    }
}
