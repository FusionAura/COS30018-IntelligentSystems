import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
public class MainController
{
    public static void main (String[] args) throws StaleProxyException, InterruptedException
    {
        Runtime rt= Runtime.instance();

        System.out.println(MainController.class.getName() + ": Launching the platform Main Container...");
        Profile pMain= new ProfileImpl(null, 8888, null);
        pMain.setParameter(Profile.GUI, "true");
        ContainerController mainCtrl = rt.createMainContainer(pMain);

        // Create and start an agent of class Counter Agent
        System.out.println(MainController.class.getName() + ": Starting up the Master Controller...");



        AgentController DeliveryAgent= mainCtrl.createNewAgent("DeliveryAgent", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent.start();

        AgentController DeliveryAgent2= mainCtrl.createNewAgent("DeliveryAgent2", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent2.start();

        AgentController DeliveryAgent3= mainCtrl.createNewAgent("DeliveryAgent3", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent3.start();

        AgentController agentCtrl= mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), new Object[0]);
        agentCtrl.start();

        /*try
        {

            System.out.println(MainController.class.getName() + ": Setup Master Agent");
            MasterRoutingAgent o2a = agentCtrl.getO2AInterface(MasterRoutingAgent.class);
            o2a.setup();// Wait for some time


        }
        catch (StaleProxyException e)
        {
            e.printStackTrace();
        }*/
    }
}
