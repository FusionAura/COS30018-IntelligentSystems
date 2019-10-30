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

public class MasterRoutingAgent extends Agent implements MasterRoutingAgentInterface
{
    public static final String DELIVERY_ROUTE_ONTOLOGY = "delivery-route";
    public static String GET_CAPACITIY_REQUSET_ONTOLOGY = "capacity-request";
    public static String GET_CAPACITY_RESPONSE_ONTOLOGY = "capacitiy-response";

    private List<Integer> _vehicleCapacity = new ArrayList<>();
    private Position _position = new Position(100, 100);
    private List<List<Double>> _distanceMatrix = new ArrayList<List<Double>>();
    private List<Node> _allNodes = new ArrayList<Node>();
    private Integer _responses = 0;
    private List<Parcel> _allParcel = new ArrayList<Parcel>();

    protected void setup()
    {
        //https://stackoverflow.com/questions/28652869/how-to-get-agents-on-all-containers-jade
        registerO2AInterface(MasterRoutingAgentInterface.class, this);

        // Setting up message listener so that it can recieve messages from other agents
        CyclicBehaviour msgListenBehaviour = new CyclicBehaviour(this)
        {

            public void action()
            {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    if (msg.getOntology().equals(GET_CAPACITY_RESPONSE_ONTOLOGY)) {
                        synchronized (_responses) {
                            _responses++;
                            // id,capacity
                            String[] response = msg.getContent().split(",");
                            _vehicleCapacity.add(Integer.parseInt(response[0]), Integer.valueOf(response[1]));
                        }
                    }
                }
                block();
            }
        };
        addBehaviour(msgListenBehaviour);

        newNode(new Node("warehouse", _position));
    }
    public void addParcel(Parcel p) {
        _allParcel.add(p);
    }
    public void removeParcel(Parcel p) {
        _allParcel.remove(p);
    }

    private List<AID> getDeliveryAgents() {
        AMSAgentDescription[] agents;
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            agents = AMSService.search( this, new AMSAgentDescription(), c );
        }
        catch (Exception e)
        {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
            agents = new AMSAgentDescription[]{};
        }
        List<AID> deliveryAgents = new ArrayList<>();

        AID myID = getAID(); //This method to get the identity of //agents such as (Name , address , host ....etc)
        for (AMSAgentDescription agent : agents) {
            AID agentID = agent.getName();
            if (!agentID.equals(myID) && !agentID.equals("ams@10.0.0.132:8888/JADE") && !agentID.equals("MasterRoutingAgent@10.0.0.132:8888/JADE")&& !agentID.equals("df@10.0.0.132:8888/JADE"))
            //{
            //if (agentID.getName().matches("d/d*"))
            {
                System.out.println(agentID.getName());
                deliveryAgents.add(agentID);
            }
        }

        return deliveryAgents;
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
            RemoveNode(position);
        }
    }

    // Notify that a node has been removed and update the distance matrix
    public void RemoveNode(int position) {
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

    @Override
    public void startRouting() {
        try {
            SendRoutes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GetCapacity(List<AID> deliveryAgents) {
        for(int i = 0; i <deliveryAgents.size(); i++)
        {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setLanguage("English");
            msg.setOntology(GET_CAPACITIY_REQUSET_ONTOLOGY);
            msg.addReceiver(deliveryAgents.get(i));
            msg.setContent(String.valueOf(i));
            System.out.print(msg);
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

    private void SendRoutes() throws IOException {
        List<AID> deliveryAgents = getDeliveryAgents();
        List<Routing.Routes> newRoute;
        Routing VRPRoute = new Routing();
        GetCapacity(deliveryAgents);

        List<Integer> demands = new ArrayList<>(_distanceMatrix.size());
        List<Integer> parcelWeight = new ArrayList<>();
        for (Parcel parcel : _allParcel) {
            Optional<Node> destination = _allNodes.stream()
                    .filter(node -> node.amI(parcel.getDestination()))
                    .findFirst();
            if (destination.isPresent()) {
               int index = _allNodes.indexOf(destination.get());
                demands.add(index, 1);
                parcelWeight.add(parcel.getWeight());
            }
        }

        Routing.DataModel dataModel = new Routing.DataModel(
                _distanceMatrix,
                deliveryAgents.size(),
                demands,
                _vehicleCapacity,
                0,
                parcelWeight
        );

        //TODO: Generate the routes from the Routing class and send them to each delivery agent
        //TODO: Loop currently sends one test route to each delivery agent

        newRoute = VRPRoute.VRP(dataModel);

        for (int i =0;i<deliveryAgents.size();i++)
        {
            List<Node> testRoute = new ArrayList<Node>();

            AID agent = deliveryAgents.get(i);

            for (int nodePos : newRoute.get(i).route)
            {
                testRoute.add(_allNodes.get(nodePos));
            }

            MessageObject msgObject = new MessageObject();
            msgObject.SetRoute(testRoute);

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setLanguage("English");

            msg.setOntology(DELIVERY_ROUTE_ONTOLOGY);

            msg.setContentObject(msgObject);
            msg.addReceiver(agent);




            send(msg);
        }
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

    public List<List<Double>> getDistanceMatrix() {
        return _distanceMatrix;
    }
}
