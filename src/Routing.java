import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/*
Initial Search Iteration
Just trying to get the shortest path 1 at a time based on the locations with demand of 1 representing parcels to deliver
to that location. as of 10/10 having issues with routes picking the same place.
Will implement constraints once the above issues are resolved to test for constraint solving.

Will then look into actually searching for multiple vehicle routing optimisation after the base is complete and working
with constraints.

 */

public class Routing {

    static class DataModel {
        private final List<List<Double>> _distanceMatrix;
        private final int _vehicleNumber;
        //demand of each node 0 for 0 as that is our depot, and 1 for every other node location because we want to visit
        //them once only
        private List<Integer> _demands;
        //vehicle capacity not extending weight yet
        private final List<Integer> _vehicleCapacities;
        private final int _depot;
        private final int _packages;

        public DataModel(
                List<List<Double>> distanceMatrix,
                int vehicleNumber,
                List<Integer> demands,
                List<Integer> vehicleCapacities,
                int depot,
                int packages) {
            _distanceMatrix = distanceMatrix;
            _vehicleNumber = vehicleNumber;
            _demands = demands;
            _vehicleCapacities = vehicleCapacities;
            _depot = depot;
            _packages = packages;
        }

        public List<List<Double>> getDistanceMatrix() {
            return _distanceMatrix;
        }

        public int getVehicleNumber() {
            return _vehicleNumber;
        }

        public List<Integer> getDemands() {
            return _demands;
        }

        public List<Integer> getVehicleCapacities() {
            return _vehicleCapacities;
        }

        public int getDepot() {
            return _depot;
        }

        public int getPackages() {
            return _packages;
        }
    }

    static class Routes
    {
        public int vehicleID;
        public List<Integer> route;
        public double routeCost;
        public Routes()
        {
        }
    }

    public static List<Routes> VRP(DataModel data)
    {
        List<Routes> RoutingManager = new ArrayList<>();
        //get the ideal best route fastest route considering packages total / car total
        //vehicles must return to depot thus extra distance added at the end
        double bestPath = 0;
        //long to truncate round down
        //should check load fits vehicleCap leaving it out for now
        //while loop while there is still demands = 1 meaning parcels to deliver
        int currentVehicle = 0;
        int test = 3;
        //Searching loop start
        while (test>0)
        {
            //creating/initializing vars to be used for our search
            Routes route = new Routes();
            boolean found = false;
            List<Integer> currentRoute = new ArrayList<>();
            currentRoute.clear();
            //starting point of Routes == depot
            currentRoute.add(0);
            List<Integer> locations = new ArrayList<>();
            //get the locations with demands (visit once)
            //data.demands index = location index, value = (0,1) for delivery required
            //converting to locations.list values contains location index
            for (int d = 0; d < data.getDemands().size(); d++)
            {
//                System.out.println("Add Loc:"+d + " demands:"+data.demands.get(d));
                //for every
                if (data.getDemands().get(d) == 1)
                {
                    locations.add(d);
                }
            }
            //loop to add routes while locations to delivery >0
            while (!found && (locations.size()>0))
            {
                //double to hold our cost
                double currentCost = 0;
                //determining amount of packages a car holds going from least necessary to most by using truncation math.floor
                if (data.getVehicleNumber()*data.getVehicleCapacities().get(0) < data.getPackages())
                    System.out.println("Capacity < parcels to delivery in 1 run"+ RoutingManager.size());
                //int load = (int)Math.floor(parcels/data.vehicleNumber);
                //using simple load=6 debugging
                int load = data.getVehicleCapacities().get(currentVehicle);
                if (load> data.getVehicleCapacities().get(currentVehicle))
                    System.out.println("Load Error , above car capacity:"+load +data.getVehicleCapacities().get(currentVehicle));
                //load is used to loop vehicle routing incase load>location size
                if (locations.size() < load)
                    load = locations.size();
                //current base just gets the shortest route until capacity reached a multi travelling salesmen approach
                //for testing
                //loop to find a route for load number of times
                for(int i = 0; i < load; i++)
                {
                    int bestJ=0;
                    double bestCost = 999999;
                    if (locations.size()>0)
                    {
                        //get the lowest cost path form the last entry to currentRoute (current location)
                        for (int j = 0; j < locations.size(); j++)
                        {
                            double current = data.getDistanceMatrix().get(currentRoute.get(currentRoute.size()-1)).get(locations.get(j));
                            if (current < bestCost)
                            {
                                bestCost = current;
                                bestJ = locations.get(j);
                            }
                        }
                    }
                    currentRoute.add(bestJ);
                    currentCost += bestCost;
//                    System.out.println("Appending:"+bestJ+ " total:"+currentCost + " removing Loc:"+locations.indexOf(bestJ));
//                    System.out.println("CRsize:"+currentRoute.size() + " load:"+i);
                    locations.remove(locations.indexOf(bestJ));
//                    System.out.println(locations.size()+"locSize");
                }
                //check that the search is complete
                if (currentRoute.size()== load+1 || currentRoute.size() == locations.size())
                {
                    //must return to depot so add this into routing and costs
                    currentCost += data.getDistanceMatrix().get(currentRoute.get(currentRoute.size()-1)).get(0);
                    currentRoute.add(0);
//                    System.out.println("Loading data%ncar:"+currentVehicle+"%nRoute:"+currentRoute+ "%nCost:"+currentCost);
                    //update Route class
                    route.vehicleID = currentVehicle;
                    route.routeCost = currentCost;
                    route.route = currentRoute;
                    //Update demands set demands index of route elements to 0
                    for (int k = 0; k<currentRoute.size(); k++)
                    {
//                        System.out.println("cRk:"+currentRoute.get(k)+ " demandsK:"+data.demands.get(currentRoute.get(k)));
                        data.getDemands().set(currentRoute.get(k),0);
                    }
                    //update vars for while(demands) loop
                    if (currentVehicle < 3)
                        currentVehicle++;
                    //break route loop
                    found = true;
                }
            }
            RoutingManager.add(route);
            //placeholder var to run our search loop 3 times for simplicity
            test--;
        }
        return RoutingManager;
    }

    public static void main(String [] args)
    {
        List<List<Double>> distanceMatrix = new ArrayList<>();
        distanceMatrix.add(Arrays.asList(0d, 548d, 776d, 696d, 582d, 274d, 502d, 194d, 308d, 194d, 536d, 502d, 388d, 354d, 468d, 776d, 662d));
        distanceMatrix.add(Arrays.asList(548d, 0d, 684d, 308d, 194d, 502d, 730d, 354d, 696d, 742d, 1084d, 594d, 480d, 674d, 1016d, 868d, 1210d));
        distanceMatrix.add(Arrays.asList(776d, 684d, 0d, 992d, 878d, 502d, 274d, 810d, 468d, 742d, 400d, 1278d, 1164d, 1130d, 788d, 1552d, 754d));
        distanceMatrix.add(Arrays.asList(696d, 308d, 992d, 0d, 114d, 650d, 878d, 502d, 844d, 890d, 1232d, 514d, 628d, 822d, 1164d, 560d, 1358d));
        distanceMatrix.add(Arrays.asList(582d, 194d, 878d, 114d, 0d, 536d, 764d, 388d, 730d, 776d, 1118d, 400d, 514d, 708d, 1050d, 674d, 1244d));
        distanceMatrix.add(Arrays.asList(274d, 502d, 502d, 650d, 536d, 0d, 228d, 308d, 194d, 240d, 582d, 776d, 662d, 628d, 514d, 1050d, 708d));
        distanceMatrix.add(Arrays.asList(502d, 730d, 274d, 878d, 764d, 228d, 0d, 536d, 194d, 468d, 354d, 1004d, 890d, 856d, 514d, 1278d, 480d));
        distanceMatrix.add(Arrays.asList(194d, 354d, 810d, 502d, 388d, 308d, 536d, 0d, 342d, 388d, 730d, 468d, 354d, 320d, 662d, 742d, 856d));
        distanceMatrix.add(Arrays.asList(308d, 696d, 468d, 844d, 730d, 194d, 194d, 342d, 0d, 274d, 388d, 810d, 696d, 662d, 320d, 1084d, 514d));
        distanceMatrix.add(Arrays.asList(194d, 742d, 742d, 890d, 776d, 240d, 468d, 388d, 274d, 0d, 342d, 536d, 422d, 388d, 274d, 810d, 468d));
        distanceMatrix.add(Arrays.asList(536d, 1084d, 400d, 1232d, 1118d, 582d, 354d, 730d, 388d, 342d, 0d, 878d, 764d, 730d, 388d, 1152d, 354d));
        distanceMatrix.add(Arrays.asList(502d, 594d, 1278d, 514d, 400d, 776d, 1004d, 468d, 810d, 536d, 878d, 0d, 114d, 308d, 650d, 274d, 844d));
        distanceMatrix.add(Arrays.asList(388d, 480d, 1164d, 628d, 514d, 662d, 890d, 354d, 696d, 422d, 764d, 114d, 0d, 194d, 536d, 388d, 730d));
        distanceMatrix.add(Arrays.asList(354d, 674d, 1130d, 822d, 708d, 628d, 856d, 320d, 662d, 388d, 730d, 308d, 194d, 0d, 342d, 422d, 536d));
        distanceMatrix.add(Arrays.asList(468d, 1016d, 788d, 1164d, 1050d, 514d, 514d, 662d, 320d, 274d, 388d, 650d, 536d, 342d, 0d, 764d, 194d));
        distanceMatrix.add(Arrays.asList(776d, 868d, 1552d, 560d, 674d, 1050d, 1278d, 742d, 1084d, 810d, 1152d, 274d, 388d, 422d, 764d, 0d, 798d));
        distanceMatrix.add(Arrays.asList(662d, 1210d, 754d, 1358d, 1244d, 708d, 480d, 856d, 514d, 468d, 354d, 844d, 730d, 536d, 194d, 798d, 0d));

        List<Integer> demands = Arrays.asList(0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1);
        DataModel data = new DataModel(
                distanceMatrix,
                3,
                demands,
                Arrays.asList(6, 6, 6),
                0,
                demands.size()
        );
        List<Routes> RoutingManager = VRP(data);
        for(int i =0; i<RoutingManager.size(); i++)
        {
            System.out.println("Vehicle:"+i +"%nPathing to:"+RoutingManager.get(i).route+"%nDistance:"+RoutingManager.get(i).routeCost);
        }
    }
}