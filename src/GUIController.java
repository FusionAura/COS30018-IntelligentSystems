import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class GUIController {
    //List View variables.
    @FXML
    public ListView AgentsList;
    @FXML
    public Button CreateButton;

    @FXML
    public void createAgent(Event e) {
        System.out.println("HI LADS");
    }
}
