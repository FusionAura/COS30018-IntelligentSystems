import java.io.Serializable;
import java.util.List;

public class MessageObject implements Serializable {
    private List<Node> _route;

    public void setRoute(List<Node> route){
        _route = route;
    }

    public List<Node> getRoute(){
        return _route;
    }
}
