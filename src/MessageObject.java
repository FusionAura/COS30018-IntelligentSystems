import java.io.Serializable;
import java.util.List;

public class MessageObject implements Serializable {
    private List<Routing.Routes> _route;

    public void SetRoute(List<Routing.Routes> route){
        _route = route;
    }

    public List<Routing.Routes> GetRoute(){
        return _route;
    }
}
