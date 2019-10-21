import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class DeliveryAgent extends Agent implements Drawable
{
    private List<Node> _route = new ArrayList<Node>();
    private double _speed = 20;
    private boolean _isTraveling = false;
    private Circle _body = null;
    private int _radius = 400; //in meters, same as distance matrix

    protected void setup()
    {
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    //Ontology check exists if we need to send different types of messages
                    if(msg.getOntology().equals(MasterRoutingAgent.DELIVERY_ROUTE_ONTOLOGY))
                    {
                        if (_isTraveling) {
                            System.out.println("Got a route, but I'm Travelling! Ignoring...");
                        } else {
                            try {
                                System.out.println("Delivery route message received!");
                                MessageObject msgObject = (MessageObject) msg.getContentObject();
                                _route = msgObject.GetRoute();
                                System.out.println("First route node coordinates: "+_route.get(0).getX()+", "+_route.get(0).getY());
                                System.out.println("Delivery route successfully added!");
                                FollowRoute();
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        Object[] args = getArguments();
        _body = (Circle) args[0];

        GetNodesInRadius();
    }

    // When this method is called, the delivery agent moves towards its next destination by deltaTime (if it has one)
    public void FollowRoute() {
        if (_route.size() == 0 || _body == null) {
            return;
        }
        _isTraveling = true;

        List<TranslateTransition> transitions = new ArrayList<>();
        for (int i = 0; i < _route.size(); i++) {
            Node thisNode = _route.get(i);
            Node nextNode = _route.size() == i + 1 ? _route.get(0) : _route.get(i + 1);

            //TODO -- Update delivery agent's current position node

            double xTransition = nextNode.getX() - thisNode.getX();
            double yTransition = nextNode.getY() - thisNode.getY();

            double distance = Math.sqrt(Math.pow(xTransition, 2) + Math.pow(yTransition, 2));
            double seconds = distance / _speed;

            TranslateTransition transition = new TranslateTransition(Duration.seconds(seconds), _body);
            transition.setByX(xTransition);
            transition.setByY(yTransition);

            transitions.add(transition);
        }
        synchronized (DeliveryAgent.class) {
            SequentialTransition sequentialTransition = new SequentialTransition(transitions.toArray(new TranslateTransition[transitions.size()]));
            sequentialTransition.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    _isTraveling = false;
                }
            });
            sequentialTransition.play();
        }
    }

    @Override
    public void Draw() {
        _route.add(new Node("AS", new Position(100, 100)));
        _route.add(new Node("AS", new Position(167.24, 98.35)));
        FollowRoute();
    }
    @Override
    public void GetAgent() {

    }

    //TODO -- Implement delivery agent current position system
    //based on distance matrix instead of hard coding
    public List<Node> GetNodesInRadius()
    {
        List<Node> nodesInRadius = new ArrayList<Node>();
        int index = 0;

        for(double d : Routing.DataModel.distanceMatrix[0])
        {
            if(d<=_radius)
            {
                //Node position Y indicates number of node, so node with pos Y of 8
                //means the node 8 is within the radius of the agent, x,y based on
                //distance matrix
                nodesInRadius.add(new Node("", new Position(0, index)));
            }
            index++;
        }

        return nodesInRadius;
    }

}


