import java.util.ArrayList;
import java.util.List;

public class DataModel {
    private final List<List<Double>> _distanceMatrix;
    private final int _numberOfVehicles;
    //demand of each node 0 for 0 as that is our depot, and 1 for every other node location because we want to visit
    //them once only
    private List<Integer> _demands;
    private List<Integer> _demandsCopy;
    //vehicle capacity not extending weight yet
    private final List<Integer> _vehicleCapacities;
    private final int _depot;
    private final List<Integer> _parcelWeight;

    public DataModel(
            List<List<Double>> distanceMatrix,
            int numberOfVehicles,
            List<Integer> demands,
            List<Integer> vehicleCapacities,
            int depot,
            List<Integer> parcelWeight) {
        _distanceMatrix = distanceMatrix;
        _numberOfVehicles = numberOfVehicles;
        _demands = demands;
        _demandsCopy = List.copyOf(_demands);
        _vehicleCapacities = vehicleCapacities;
        _depot = depot;
        _parcelWeight = parcelWeight;
    }

    public Double getDistance(int node1, int node2) {
        return _distanceMatrix.get(node1).get(node2);
    }

    public int getDistanceMatrixSize() {
        return _distanceMatrix.size();
    }

    public int getNumberOfVehicles() {
        return _numberOfVehicles;
    }

    public List<Integer> getDemands() {
        return _demands;
    }

    public Integer getVehicleCapacity(int vehicleNumber) {
        return _vehicleCapacities.get(vehicleNumber);
    }

    public void setVehicleCapacity(int vehicleNumber, int vehicleCapacity) {
        _vehicleCapacities.set(vehicleNumber, vehicleCapacity);
    }

    public int getDepot() {
        return _depot;
    }

    public Integer getParcelWeight(int parcelNumber) {
        return _parcelWeight.get(parcelNumber);
    }

    public int getPackages() {
        return _parcelWeight.size();
    }

    public void resetDemands() {
        _demands = new ArrayList<>(List.copyOf(_demandsCopy));
    }
}