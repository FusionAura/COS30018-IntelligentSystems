import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;



public class Routing {

    static class DataModel {
        public final double[][] distanceMatrix = {
                {0, 548, 776, 696, 582, 274, 502, 194, 308, 194, 536, 502, 388, 354, 468, 776, 662},
                {548, 0, 684, 308, 194, 502, 730, 354, 696, 742, 1084, 594, 480, 674, 1016, 868, 1210},
                {776, 684, 0, 992, 878, 502, 274, 810, 468, 742, 400, 1278, 1164, 1130, 788, 1552, 754},
                {696, 308, 992, 0, 114, 650, 878, 502, 844, 890, 1232, 514, 628, 822, 1164, 560, 1358},
                {582, 194, 878, 114, 0, 536, 764, 388, 730, 776, 1118, 400, 514, 708, 1050, 674, 1244},
                {274, 502, 502, 650, 536, 0, 228, 308, 194, 240, 582, 776, 662, 628, 514, 1050, 708},
                {502, 730, 274, 878, 764, 228, 0, 536, 194, 468, 354, 1004, 890, 856, 514, 1278, 480},
                {194, 354, 810, 502, 388, 308, 536, 0, 342, 388, 730, 468, 354, 320, 662, 742, 856},
                {308, 696, 468, 844, 730, 194, 194, 342, 0, 274, 388, 810, 696, 662, 320, 1084, 514},
                {194, 742, 742, 890, 776, 240, 468, 388, 274, 0, 342, 536, 422, 388, 274, 810, 468},
                {536, 1084, 400, 1232, 1118, 582, 354, 730, 388, 342, 0, 878, 764, 730, 388, 1152, 354},
                {502, 594, 1278, 514, 400, 776, 1004, 468, 810, 536, 878, 0, 114, 308, 650, 274, 844},
                {388, 480, 1164, 628, 514, 662, 890, 354, 696, 422, 764, 114, 0, 194, 536, 388, 730},
                {354, 674, 1130, 822, 708, 628, 856, 320, 662, 388, 730, 308, 194, 0, 342, 422, 536},
                {468, 1016, 788, 1164, 1050, 514, 514, 662, 320, 274, 388, 650, 536, 342, 0, 764, 194},
                {776, 868, 1552, 560, 674, 1050, 1278, 742, 1084, 810, 1152, 274, 388, 422, 764, 0, 798},
                {662, 1210, 754, 1358, 1244, 708, 480, 856, 514, 468, 354, 844, 730, 536, 194, 798, 0},
        };
        public final int vehicleNumber = 3;
        //demand of each node 0 for 0 as that is our depot, and 1 for every other node location because we want to visit
        //them once only
        public List<Integer> demands = Arrays.asList(0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1);
        //vehicle capacity not extending weight yet
        public final long[] vehicleCapacities = {6,6,6};
        public final int depot = 0;
        public final int packages = this.distanceMatrix.length;
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

    public static List<Routes> VRP()
    {
        List<Routes> RoutingManager = new ArrayList<>();
        DataModel data = new DataModel();
        //get the ideal best route fastest route considering packages total / car total
        //vehicles must return to depot thus extra distance added at the end
        double bestPath = 0;
        //long to truncate round down
        //should check load fits vehicleCap leaving it out for now
        //while loop while there is still demands = 1 meaning parcels to deliver
        int currentVehicle = 0;
        int test = 3;
        while (test>0)
        {
            System.out.println("While");
            Routes route = new Routes();
            boolean found = false;
            List<Integer> currentRoute = new ArrayList<>();
            currentRoute.clear();
            //starting point of Routes == depot
            currentRoute.add(0);
            System.out.println("While"+ currentRoute.size());
            int parcels = 0;
            for (int p = 0; p<data.demands.size();p++)
            {
                if (data.demands.get(p) == 1)
                    parcels ++;
            }
            List<Integer> locations = new ArrayList<>();
            //get the locations with demands
            for (int d = 0; d < data.demands.size()-1; d++)
            {
                if (data.demands.get(d) == 1)
                    locations.add(d);
            }
            while (!found)
            {
                double currentCost = 0;
                //determining amount of packages a car holds going from least necessary to most by using truncation math.floor
                if (data.vehicleNumber*data.vehicleCapacities[0] < data.packages)
                    System.out.println("Capacity < parcels to delivery in 1 run"+ RoutingManager.size());
                //int load = (int)Math.floor(parcels/data.vehicleNumber);
                //using simple load=6 debugging
                int load = 6;
                if (load> data.vehicleCapacities[0])
                    System.out.println("Load Error , above car capacity:"+load +data.vehicleCapacities[currentVehicle]);
                //current base just gets the shortest route until capacity reached a multi travelling salesmen approach
                //for testing
                System.out.println("Finding...%nload:"+load+ "for car:"+currentVehicle);
                for(int i = 0; i < load; i++)
                {
                    //System.out.println("locSize:"+locations.size()+" i:"+i + " lastLoc:"+currentRoute.get(currentRoute.size()-1));
                    int bestJ=0;
                    double bestCost = 999999;
                    //get the lowest cost path form the last entry to currentRoute (current location)
                    for (int j = 0; j < locations.size()-1; j++)
                    {
                        double current = data.distanceMatrix[currentRoute.get(currentRoute.size()-1)][locations.get(j)];
                        System.out.println("CurrRoute:"+currentRoute.get(currentRoute.size()-1) + " j:"+locations.get(j)+" current should be:"+current + " <= "+bestCost);
                        //System.out.println("j:"+j+" current:"+current);
                        if (current <= bestCost)
                        {
                            System.out.println("current:"+current+" bestCost:"+bestCost + "j:"+j);
                            bestCost = current;
                            bestJ = locations.get(j);
                            System.out.println("current:"+current+" bestCost:"+bestCost + "bestJ:"+bestJ);
                        }
                    }
                    currentRoute.add(bestJ);
                    currentCost += bestCost;
                    System.out.println("Appending:"+bestJ+ " total:"+currentCost);
                    System.out.println("CRsize:"+currentRoute.size() + " load:"+load);
                }
                if (currentRoute.size()== load+1 || currentRoute.size() == locations.size())
                {
                    System.out.println("Loaded:"+currentRoute.size());
                    //must return to depot so add this into routing and costs
                    currentCost += data.distanceMatrix[currentRoute.get(currentRoute.size()-1)][0];
                    currentRoute.add(0);
                    //break loop and update Route class
                    found = true;
                    System.out.println("Loading data%ncar:"+currentVehicle+"%nRoute:"+currentRoute+ "%nCost:"+currentCost);
                    route.vehicleID = currentVehicle;
                    route.routeCost = currentCost;
                    route.route = currentRoute;
                    //Update demands set demands index of route elements to 0
                    for (int k = 0; k<currentRoute.size(); k++)
                    {
                        data.demands.set(currentRoute.get(k),0);
                    }
                    //update vars for while(demands) loop
                    if (currentVehicle < 3)
                        currentVehicle++;
                }
            }
            System.out.println("found:test:"+test);
//            System.out.println("print start");
//            for (int n=0; n<data.demands.size();n++)
//            {
//                System.out.println("print demands:"+data.demands.get(n));
//            }
//            System.out.println("print end");
//            System.out.println("Demands:"+data.demands.contains(1));
//            //Update routes before repeating
//            System.out.println("append RoutingManager");
            RoutingManager.add(route);
            test--;
        }
        return RoutingManager;
    }

    public static void main(String [] args)
    {

        List<Routes> RoutingManager = VRP();
        for(int i =0; i<RoutingManager.size(); i++)
        {
            System.out.println("Vehicle:"+i +"%nPathing to:"+RoutingManager.get(i).route+"%nDistance:"+RoutingManager.get(i).routeCost);
        }
    }
}