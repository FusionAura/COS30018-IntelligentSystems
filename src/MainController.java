import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

public class MainController extends Application {
    @FXML
    public ListView _agentList;

    private List<Node> _nodes = new ArrayList<>();
    private List<Parcel> _parcels = new ArrayList<>();
    private GUIController _guiController;
    private AgentController _mainAgentController;
    private ContainerController _mainCtrl;


    private Color[] _deliveryColors = {
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.RED,
            Color.AQUA,
            Color.LIME,
            Color.GOLD,
            Color.CRIMSON
    };
    private int _deliveryColorPosition = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Load GUI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("IntelligentSystems.fxml"));
        Parent root = loader.load(); // must be called before getting the controller!
        _guiController = loader.getController();
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
        Runtime rt = Runtime.instance();
        System.out.println(MainController.class.getName() + ": Launching the platform Main Container...");
        Profile pMain = new ProfileImpl(null, 8888, null);

        //pMain.setParameter(Profile.GUI, "true");
        _mainCtrl = rt.createMainContainer(pMain);


        // Create and start an agent of class Counter Agent
        System.out.println(MainController.class.getName() + ": Starting up the Master Controller...");
        
        _mainAgentController = _mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), new Object[0]);
        _mainAgentController.start();
        //Register Master Routing position
        _guiController.RegisterCircle(new Circle(100, 100, 10, Color.CHOCOLATE));

         /*AID AgentMaster = new AID(_mainAgentController.getName(),false);
       JadeGateway.execute(new OneShotBehaviour() {
            @Override
            public void action() {
                final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

                msg.addReceiver(AgentMaster);
                msg.setContent(STARTROUTE);
                myAgent.send(msg);
            }
        });*/

        //MasterRoutingAgent.

        //JadeGateway.

        readFromConfigFile();

        //Populate GUI ListView
        _guiController.PopulateAgentList();
        
        _guiController.MainClass = this;
    }

    public void runAction() {
        try {
            MasterRoutingAgentInterface masterRoutingAgentInterface = _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class);
            masterRoutingAgentInterface.StartRouting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main (String[] args) throws StaleProxyException
    {
        launch(args);
    }


    private void readFromConfigFile() {
        CSVFileReader reader = new CSVFileReader();
        List<List<String>> output = reader.readFromFile("app.config");

        for (List<String> line : output) {
            String type = line.get(0);

            switch (type) {
                case "S":
                    // General settings such as number of delivery agents.
                    // S,numOfDeliveryAgents
                    int numOfDeliveryAgents = Integer.parseInt(line.get(1));
                    for (int i = 1; i <= numOfDeliveryAgents; i++) {
                        try {
                            Circle agentBody = new Circle(100, 100, 5, _deliveryColors[_deliveryColorPosition]);
                            _deliveryColorPosition++;
                            if (_deliveryColorPosition == _deliveryColors.length) {
                                _deliveryColorPosition = 0;
                            }

                            _guiController.RegisterCircle(agentBody);

                            AgentController newDeliveryAgent= _mainCtrl.createNewAgent("d" + i, DeliveryAgent.class.getName(), new Object[] {agentBody});
                            newDeliveryAgent.start();
                            _guiController.DoList.add(newDeliveryAgent.getName());
                        } catch (StaleProxyException e) {
                            e.printStackTrace();
                        }

                    }
                    break;

                case "N":
                    // Here the node is created. Master routing agent must be notified.
                    // N,posX,posY,nodeName
                    Position nodePosition = new Position(Double.parseDouble(line.get(1)), Double.parseDouble(line.get(2)));

                    Node node = new Node(line.get(3), nodePosition);
                    _guiController.RegisterCircle(node.getBody());
                    _nodes.add(node);

                    try {
                        _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class).NewNode(node);
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }

                    break;

                case "P":
                    // Add in another parcel. Must have a destination.
                    // P,weight,destination
                    Parcel parcel = new Parcel(line.get(2), Integer.parseInt(line.get(1)));
                    _parcels.add(parcel);
                    break;

                default:
                    System.out.println("Invalid line, ignoring");
            }
        }
    }
}
