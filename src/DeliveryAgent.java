import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.tools.sniffer.Message;

import java.util.ArrayList;
import java.util.List;

public class DeliveryAgent extends Agent implements Drawable
{
    private List<Node> _route = new ArrayList<Node>();
    private long _speed = 5;
    private Position _position = new Position(0, 0);

    protected void setup()
    {
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    try {
                        MessageObject msgObject = (MessageObject) msg.getContentObject();
                        _route = msgObject.GetRoute();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    System.out.println(getLocalName()+" Received message "+msg.getContent()+" from "+msg.getSender().getLocalName());
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Agent "+getLocalName()+" responding!");
                    System.out.println(getLocalName()+": Sending response "+reply.getContent()+" to "+msg.getAllReceiver().next());
                    send(reply);
                    System.out.println(_route.get(0).getX());
                }
            }
        });
    }

    // When this method is called, the delivery agent moves towards its next destination by deltaTime (if it has one)
    public void FollowRoute(long deltaTime) {
        if (_route.isEmpty()) {
            return;
        }
        Node nextLocation = _route.get(0);
        // Get the angle of X and Y
        double deltaX = Math.atan( nextLocation.getY()/nextLocation.getX() );
        double deltaY = Math.atan( nextLocation.getX()/nextLocation.getY() );

        // Account for how fast the agent will move
        deltaX *= _speed * deltaTime;
        deltaY *= _speed * deltaTime;

        // If we will arrive at the location (or go too far) then set our position to that location
        // Otherwise move closer
        int xCompare = Double.compare(_position.getX(), nextLocation.getX());
        int yCompare = Double.compare(_position.getY(), nextLocation.getY());
        int deltaXCompare = Double.compare(_position.getX() + deltaX, nextLocation.getX());
        int deltaYCompare = Double.compare(_position.getY() + deltaY, nextLocation.getY());

        if (xCompare != deltaXCompare) {
            _position.setX(nextLocation.getX());
        } else {
            _position.setX(deltaX + _position.getX());
        }

        if (yCompare != deltaYCompare) {
            _position.setY(nextLocation.getY());
        } else {
            _position.setY(deltaY + _position.getY());
        }

        // If we have arrived then move onto the next location
        if (_position.getX() == nextLocation.getX() && _position.getY() == nextLocation.getY()) {
            _route.remove(0);
        }
    }

    @Override
    public void Draw() {

    }
}
