

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.ListView;

public class MainController extends Application
{
    //List View variables.
    @FXML private ListView AgentsList;
    @FXML private Button CreateButton;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("IntelligentSystems.fxml"));
        primaryStage.setTitle("Intelligent Systems Agent Program");
        primaryStage.setScene(new Scene (root));
        primaryStage.show();
        Runtime rt= Runtime.instance();

        System.out.println(MainController.class.getName() + ": Launching the platform Main Container...");
        Profile pMain= new ProfileImpl(null, 8888, null);
        //pMain.setParameter(Profile.GUI, "true");
        ContainerController mainCtrl = rt.createMainContainer(pMain);

        // Create and start an agent of class Counter Agent
        System.out.println(MainController.class.getName() + ": Starting up the Master Controller...");

        AgentController DeliveryAgent= mainCtrl.createNewAgent("d1", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent.start();

        AgentController DeliveryAgent2= mainCtrl.createNewAgent("d2", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent2.start();

        AgentController DeliveryAgent3= mainCtrl.createNewAgent("d3", DeliveryAgent.class.getName(), new Object[0]);
        DeliveryAgent3.start();

        AgentController agentCtrl= mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), new Object[0]);
        agentCtrl.start();

        //Populate List
        //AgentsList.getItems().addAll("");


    }

    public static void main (String[] args) throws StaleProxyException
    {
        launch(args);
    }

}
