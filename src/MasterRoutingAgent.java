import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MasterRoutingAgent extends Agent implements Drawable
{
    private int capacity;
    private Position _position = new Position(0, 0);
    private List<List<Double>> _distanceMatrix = new ArrayList<List<Double>>();

    protected void setup()
    {
        CyclicBehaviour msgListenBehaviour = new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    System.out.println(getLocalName()+": Received response "+msg.getContent()+" from "+msg.getSender().getLocalName());
                }
                block();
            }
        };
        addBehaviour(msgListenBehaviour);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("Contacting agents...");
        for(int i=1;i<=3;i++)
        {
            msg.addReceiver(new AID("d"+i, AID.ISLOCALNAME));
        }

        System.out.println(getLocalName()+": Sending message "+msg.getContent()+" to ");
        Iterator receivers = msg.getAllIntendedReceiver();
        while(receivers.hasNext())
        {
            System.out.println(((AID)receivers.next()).getLocalName());
        }
        send(msg);
    }

    // Notify that a new node has been created and update the distance matrix
    public void NewNode(Node newNode, List<Node> allOtherNodes) {
        List<Double> distanceBetweenNodes = new ArrayList<>();
        for (int i = 0; i < allOtherNodes.size(); i++) {
            // Calculate distance using trigonometry
            double xDifference = _position.getX() - allOtherNodes.get(i).getX();
            double yDifference = _position.getY() - allOtherNodes.get(i).getY();

            // a^2 + b^2 = c^2
            Double distance = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));

            distanceBetweenNodes.add(distance);
            _distanceMatrix.get(i).add(distance);
        }

        // Add the distance to itself (used in matrix)
        distanceBetweenNodes.add((double) 0);

        _distanceMatrix.add(distanceBetweenNodes);
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
    }

    @Override
    public void Draw() {
        
    }
}
