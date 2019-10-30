import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
Initial Search Iteration
Just trying to get the shortest path 1 at a time based on the locations with demand of 1 representing parcels to deliver
to that location. as of 10/10 having issues with routes picking the same place.
Will implement constraints once the above issues are resolved to test for constraint solving.

Will then look into actually searching for multiple vehicle routing optimisation after the base is complete and working
with constraints.

 */

public class Routing {

    static class Routes {
        public int vehicleID;
        public List<Integer> route = new ArrayList<>();
        public double routeCost;
        public Routes() {
        }
        public Routes(int pId, int pStartingLoc)
        {
            vehicleID = pId;
            route.add(pStartingLoc);
        }
    }

    //Neighbouring Location identifier
    public static  List<Integer> NegativeDomain(double pRange, DataModel pData, List<Integer> pDomain , Integer pLoc)
    {
        List<Integer> negativeDomain = new ArrayList<>();
        for (int i = 0; i< pDomain.size();i++)
        {
            if (pData.getDistance(pLoc, pDomain.get(i)) < pRange)
            {
                negativeDomain.add(pDomain.get(i));
            }
        }
        return negativeDomain;
    }



    //find the bestPath assign to appropriate vehicles based on their position, if all same priotitize index 0->x
    //consideration to remove DataModel from param for performance.
    //pLoc = current vehicle locations, pDomain = nodes to visit/deliver
    public static List<Integer> BestNext(List<Integer> pLoc, List<Integer> pDomain, DataModel pData) {
        //index = vehicle id , value = new location
        List<Integer> returnLocs = new ArrayList<>();
        returnLocs.add(null);
        returnLocs.add(null);
        returnLocs.add(null);
        List<Integer> bestLocs = new ArrayList<>();
        List<Double> bestCosts = new ArrayList<>();
        List<List<Integer>> negativeDomain = new ArrayList<>();
        boolean found = false;
        while (!found)
        {
            //clear theste 2 lists to be searched again when 1 location is selected
            negativeDomain.clear();
            bestLocs.clear();
            bestCosts.clear();
            System.out.println("Clear");
            //vehicleNum instead of 3 later
            for (int i = 0; i < pLoc.size(); i++)
            {
                //generate negativeDomains for each vehicle
                negativeDomain.add(i,NegativeDomain(400, pData, pDomain, pLoc.get(i)));
                for(int d = 0; d<negativeDomain.size();d++) {
                    System.out.println("nDom:"+d+" at:"+pLoc.get(d) + negativeDomain.get(d));
                }
                //what needs to be found == returnLocs == null
                //vehicle index i == null. search for it
                if (returnLocs.get(i) == null) {
                    System.out.println(returnLocs.get(i) + " reloc:" + i);
                    int bestJ = 0;
                    double bestCost = 999999;
                    //get the lowest cost path form the last entry to currentRoute (current location)
                    //Weight constraint check to refine our domain space to pick
                    List<Integer> domain = new ArrayList<>();
                    domain.addAll(pDomain);
                    for(int d = 0; d<domain.size();d++)
                    {
                        System.out.println("pDomain:"+pDomain.get(d));
                        //parcel weight in the domain > vehicle capacity
                        if(pData.getVehicleCapacity(i) < pData.getParcelWeight(domain.get(d)))
                        {
                            //remove it from the search domain
                            System.out.println("load:"+pData.getVehicleCapacity(i) +" parcelWeight:"+pData.getParcelWeight(domain.get(d)));
                            System.out.println("removing domain:"+domain.get(d));
                            domain.remove(d);
                        }
                    }
                    for (int j = 0; j < domain.size(); j++) {
                        //current loc of any driver should not be in pDomain as it is visited, besides 0
                        double current = pData.getDistance(pLoc.get(i), domain.get(j));
                        //add in negativeDomain effects
                        for (int n =0; n< negativeDomain.size();n++)
                        {
                            //not ur own negativeDomain
                            if (n!=i)
                            {
                                if (negativeDomain.get(n).contains(domain.get(j)))
                                {
                                    System.out.println("negativeDomain increase:"+n+ " domJ:"+domain.get(j));
                                    current += 200;
                                }
                            }
                        }
                        if (current < bestCost) {
                            bestCost = current;
                            bestJ = pDomain.get(j);
                        }
                    }
                    //add i index bestJ and Cost as there wont always have 3 sizes to match index to vehicle numbers otherwise
                    bestLocs.add(i, bestJ);
                    bestCosts.add(i, bestCost);
                }
                // LocsReturn i has a path already set Locs/Costs i to null to ignore later
                else
                {
                    bestLocs.add(i,null);
                    bestCosts.add(i,null);
                }
            }
            System.out.println("SampleSize:");
            for (int i = 0; i< bestLocs.size(); i++)
            {
                System.out.println("loc:"+bestLocs.get(i));
                System.out.println(("costs:"+bestCosts.get(i)));
            }
            //choose the vehicle's route that has the ebst cost amongst the vehicles to add first and repeat
            System.out.println("add");
            int bestIndex = 0;
            double bestCost = 99999;
            for (int i = 0; i < bestCosts.size(); i++) {
                if (bestCosts.get(i) != null)
                {
                    if (bestCosts.get(i) < bestCost) {
                        bestIndex = i;
                        bestCost = bestCosts.get(i);
                    }
                }
            }
            System.out.println("bestI:" + bestIndex + " bestCost:" + bestCost);
            returnLocs.set(bestIndex, bestLocs.get(bestIndex));
            pDomain.remove(bestLocs.get(bestIndex));
            pData.getDemands().set(bestLocs.get(bestIndex),0);
            //Update pLoc for negativeDomain
            pLoc.set(bestIndex,bestLocs.get(bestIndex));
            //update vehicle capacity -+ parcelWeight
            pData.setVehicleCapacity(bestIndex, pData.getVehicleCapacity(bestIndex) - pData.getParcelWeight(bestLocs.get(bestIndex)));
            //returnLocs contain 3 non null locations to return found = true
            System.out.println("pDomSize:"+pDomain.size());
            if (returnLocs.contains(null) != true || pDomain.size() == 0)
                found = true;
        }
        return returnLocs;
    }

    //adds routeCost from last to new loc, add route(pNewLoc)
    public static void UpdateRouteManager(List<Routes> pRM, Integer pI, Integer pNewLoc, DataModel pData) {
        //update routeCost from current last routeindex to pNewLoc
        pRM.get(pI).routeCost += pData.getDistance(pRM.get(pI).route.size() - 1, pNewLoc);
        pRM.get(pI).route.add(pNewLoc);
    }

    public static List<Routes> VRP(DataModel data) {
        //initialize RouteManager List with 3 new Routes, add depot as first route location
        List<Routes> RouteManager = new ArrayList<>();
        for (int i =0; i< 3; i++)
        {
            Routes temp = new Routes();
            temp.vehicleID = i;
            temp.route.add(0,0);
            RouteManager.add(temp);
        }

        //our Main Search Loop
        /*

            while (vehicleLoad>0 && daomin.size()>0)
                !finished searching?
                    BestNext set of next locations each vehicle goes to
                        use Weight to shrink domain space for each vehicle
                        update vehicle location and data, mainly distance matrix based on neighbouring car deterences etc.
                        find best cost loc for each vehicle
         */
        while (data.getDemands().contains(1))
        {
            List<Integer> currentLoc = new ArrayList<>();
            List<Integer> domain = new ArrayList<>();
            for (int i = 0; i < data.getDemands().size(); i++) {
                if (data.getDemands().get(i) == 1) {
                    System.out.println("demands:"+i);
                    domain.add(i);
                }
            }
            for (int i = 0; i< RouteManager.size();i++)
            {
                currentLoc.add(RouteManager.get(i).route.get(RouteManager.get(i).route.size()-1));
            }
            List<Integer> test = BestNext(currentLoc, domain, data);
            for (int i = 0; i < test.size(); i++) {
                if (test.get(i) != null)
                {
                    UpdateRouteManager(RouteManager, i, test.get(i), data);
                }
            }
        }
        //add the final destination + cost back to warehouse
        for (int i = 0; i<RouteManager.size();i++)
        {

            UpdateRouteManager(RouteManager, i, 0, data);
        }
        for (int i = 0; i < RouteManager.size();i++)
        {
            System.out.println("RM:"+i+ RouteManager.get(i).route+ " : "+ RouteManager.get(i).routeCost + " remainingLoad:"+data.getVehicleCapacity(i));
        }

        return RouteManager;
    }

    public static void main(String[] args) {
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
                Arrays.asList(250, 200, 250),
                0,
                Arrays.asList(0,75,50,20,40,15,5,25,42,22,48,18,32,15,55,40,42)
        );
        List<Routes> RoutingManager = VRP(data);
        for(int i =0; i<RoutingManager.size(); i++)
        {
            System.out.println("Vehicle:"+i +"%nPathing to:"+RoutingManager.get(i).route+"%nDistance:"+RoutingManager.get(i).routeCost);
        }


    }
}