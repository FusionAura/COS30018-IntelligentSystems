import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class DeliveryAgent extends Agent
{
    private List<Node> _route = new ArrayList<Node>();
    private double _speed = 20;
    private int _capacity;
    private boolean _isTraveling = false;
    private Circle _body = null;

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
                                _route = msgObject.getRoute();
                                System.out.println("First route node coordinates: "+_route.get(0).getX()+", "+_route.get(0).getY());
                                System.out.println("Delivery route successfully added!");
                                followRoute();
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (msg.getOntology().equals(MasterRoutingAgent.GET_CAPACITY_REQUEST_ONTOLOGY)) {
                        ACLMessage response = new ACLMessage(ACLMessage.INFORM);
                        response.setOntology(MasterRoutingAgent.GET_CAPACITY_RESPONSE_ONTOLOGY);
                        response.setContent(String.valueOf(_capacity));
                        response.addReceiver(msg.getSender());

                        send(response);
                    }
                }
            }
        });

        Object[] args = getArguments();
        _body = (Circle) args[0];
        _capacity = (int) args[1];
        
        _body.setMouseTransparent(true);
    }

    // When this method is called, the delivery agent moves towards its next destination by deltaTime (if it has one)
    private void followRoute() {
        if (_route.size() == 0 || _body == null) {
            return;
        }
        _isTraveling = true;

        List<TranslateTransition> transitions = new ArrayList<>();
        for (int i = 0; i < _route.size(); i++) {
            Node thisNode = _route.get(i);
            Node nextNode = _route.size() == i + 1 ? _route.get(0) : _route.get(i + 1);

            double xTransition = nextNode.getX() - thisNode.getX();
            double yTransition = nextNode.getY() - thisNode.getY();

            double distance = Math.sqrt(Math.pow(xTransition, 2) + Math.pow(yTransition, 2));
            double seconds = distance / _speed;

            TranslateTransition transition = new TranslateTransition(Duration.seconds(seconds), _body);
            transition.setByX(xTransition);
            transition.setByY(yTransition);

            transitions.add(transition);
        }

        // SequentialTransitions must be created one at a time
        synchronized (SequentialTransition.class) {
            SequentialTransition sequentialTransition = new SequentialTransition(transitions.toArray(new TranslateTransition[0]));
            sequentialTransition.onFinishedProperty().set(actionEvent -> _isTraveling = false);
            sequentialTransition.play();
        }
    }
}


