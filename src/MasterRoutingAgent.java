import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import examples.content.sfo.Main;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class MasterRoutingAgent extends Agent implements Drawable
{
    private int _capacity;
    private Position _position = new Position(0, 0);
    private List<List<Double>> _distanceMatrix = new ArrayList<List<Double>>();
    private List<Node> _allNodes = new ArrayList<Node>();
    public static final String DELIVERY_ROUTE_ONTOLOGY = "Delivery-route-ontology";
    AMSAgentDescription [] agents = null;

    protected void setup()
    {
        //https://stackoverflow.com/questions/28652869/how-to-get-agents-on-all-containers-jade
        registerO2AInterface(Drawable.class, this);
        // Setting up message listener so that it can recieve messages from other agents
        CyclicBehaviour msgListenBehaviour = new CyclicBehaviour(this)
        {

            public void action()
            {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    //System.out.println(getLocalName()+": Received response "+msg.getContent()+" from "+msg.getSender().getLocalName());
                    if (msg.getContent().equals(MainController.STARTROUTE))
                    {
                        GetAgent();
                    }
                }
                block();
            }
        };
        addBehaviour(msgListenBehaviour);

        // Setting up O2A Communication so that the agent can get objects from the MainController
        setEnabledO2ACommunication(true, 0);
        CyclicBehaviour o2aListenBehaviour = new CyclicBehaviour(this) {
            @Override
            public void action() {
                Object newObject = getO2AObject();

                if (newObject != null) {
                    if (newObject instanceof Node) {
                        Node newNode = (Node)newObject;
                        NewNode(newNode);
                    }
                }
            }
        };
        addBehaviour(o2aListenBehaviour);
        /*try {
            SendRoutes();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void GetAgent()
    {
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults ( new Long(-1) );
            agents = AMSService.search( this, new AMSAgentDescription(), c );
        }
        catch (Exception e)
        {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
        }
        AID myID = getAID();// this method to get the identity of //agents such as (Name , adress , host ....etc)
        for (int i=0; i<agents.length;i++)
        {
            AID agentID = agents[i].getName();
            System.out.println(
                    ( agentID.equals( myID ) ? "*** " : "    ")
                            + i + ": " + agentID.getName()
            );
        }
    }

    // Notify that a new node has been created and update the distance matrix
    public synchronized void NewNode(Node newNode) {
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

    public synchronized void RemoveNode(Node node) {
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
    public void Draw() {
        
    }

    public void SendRoutes() throws IOException {
        //TODO -- Use JADE controller to get all delivery agent names
        //TODO -- Iterate though each existing delivery agent to send route to
        List<Node> testRoute = new ArrayList<Node>();
        testRoute.add(new Node("testNode", new Position(0, 0)));

        MessageObject msgObject = new MessageObject();
        msgObject.SetRoute(testRoute);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setLanguage("English");

        //TODO -- Define proper ontology
        msg.setOntology(DELIVERY_ROUTE_ONTOLOGY);

        msg.setContentObject(msgObject);
        msg.addReceiver(new AID("d1", AID.ISLOCALNAME));

        send(msg);
    }
    
    //MEANT ONLY FOR TESTING DISTANCE MATRIX
    public static void main(String[] args) {
        MasterRoutingAgent masterRoutingAgent = new MasterRoutingAgent();
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            Node newNode = new Node("Node" + i, new Position(rand.nextInt(100), rand.nextInt(100)));
            masterRoutingAgent.NewNode(newNode);
        }

        DecimalFormat myFormat = new DecimalFormat("##");

        for (int i = 0; i < masterRoutingAgent.get_distanceMatrix().size(); i++) {
            System.out.print("[");
            for (int j = 0; j < masterRoutingAgent.get_distanceMatrix().get(i).size(); j++) {
                System.out.print(myFormat.format(masterRoutingAgent.get_distanceMatrix().get(i).get(j)) + ", ");
            }
            System.out.println("]");
        }
    }

    public List<List<Double>> get_distanceMatrix() {
        return _distanceMatrix;
    }
}
