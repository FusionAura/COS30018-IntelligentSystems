import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

public class MainController extends Application
{
    @FXML
    public ListView AgentsList;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        //Load GUI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("IntelligentSystems.fxml"));
        Parent root = loader.load(); // must be called before getting the controller!
        GUIController controller = loader.getController();
        primaryStage.setTitle("Intelligent Systems Agent Program");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();

        //Close Button
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                System.exit(0);
            }
        });

        //JADE Startup
        Runtime rt= Runtime.instance();
        System.out.println(MainController.class.getName() + ": Launching the platform Main Container...");
        Profile pMain= new ProfileImpl();
        //pMain.setParameter(Profile.GUI, "true");
        ContainerController mainCtrl = rt.createMainContainer(pMain);


        // Create and start an agent of class Counter Agent
        System.out.println(MainController.class.getName() + ": Starting up the Master Controller...");

        List<String> deliveryAgentList = new ArrayList<String>();

        AgentController DeliveryAgent= mainCtrl.createNewAgent("d1", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent.start();
        deliveryAgentList.add(DeliveryAgent.getName().substring(0, DeliveryAgent.getName().indexOf("@")));

        AgentController DeliveryAgent2= mainCtrl.createNewAgent("d2", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent2.start();
        deliveryAgentList.add(DeliveryAgent2.getName().substring(0, DeliveryAgent2.getName().indexOf("@")));

        AgentController DeliveryAgent3= mainCtrl.createNewAgent("d3", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent3.start();
        deliveryAgentList.add(DeliveryAgent3.getName().substring(0, DeliveryAgent3.getName().indexOf("@")));

        Object[] mraArgs = new Object[1];
        mraArgs[0] = deliveryAgentList;

        AgentController agentCtrl= mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), mraArgs);
        agentCtrl.start();


        //Populate GUI ListView
        controller.DoList.add(agentCtrl.getName());
        controller.DoList.add(DeliveryAgent.getName());
        controller.DoList.add(DeliveryAgent2.getName());
        controller.DoList.add(DeliveryAgent3.getName());
        controller.PopulateAgentList();

        controller.MainClass = this;
        controller.AgentNum.setText(String.valueOf(controller.DoList.size()-1));

    }

    public static void main (String[] args) throws StaleProxyException
    {
        launch(args);
    }
}
