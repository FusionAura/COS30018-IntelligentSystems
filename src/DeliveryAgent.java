import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class DeliveryAgent extends Agent implements Drawable
{
    protected void setup()
    {
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    System.out.println(getLocalName()+" Received message "+msg.getContent()+" from "+msg.getSender().getLocalName());
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Agent "+getLocalName()+" responding!");
                    System.out.println(getLocalName()+": Sending response "+reply.getContent()+" to "+msg.getAllReceiver().next());
                    send(reply);
                }
            }
        });
    }

    @Override
    public void Draw() {

    }
}
