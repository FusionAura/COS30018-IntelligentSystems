import java.util.Iterator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MasterRoutingAgent extends Agent implements Drawable
{
    private int capacity;
    private Position _position = new Position(0, 0);

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

    @Override
    public void Draw() {
        
    }
}
