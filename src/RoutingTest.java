import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
Initial Search Iteration
Just trying to get the shortest path 1 at a time based on the locations with demand of 1 representing parcels to deliver
to that location. as of 10/10 having issues with routes picking the same place.
Will implement constraints once the above issues are resolved to test for constraint solving.

Will then look into actually searching for multiple vehicle routing optimisation after the base is complete and working
with constraints.

 */

public class RoutingTest {

    public static List<Integer> debugSelections = new ArrayList<>();

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
        public List<Integer> demands = Arrays.asList(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        //vehicle capacity not extending weight yet
        public final int[] vehicleCapacities = {200, 200, 200};
        public final int depot = 0;
        public final int[] parcelWeight = {0, 75, 50, 20, 40, 15, 5, 25, 42, 22, 48, 18, 32, 15, 55, 40, 42};
        public final int packages = this.distanceMatrix.length;
    }

    //updating Route class to represent delivery vehicles
    //to contain more information for searching
    static class Agent {
        public int vehicleID;
        public List<Integer> route;
        public double routeCost;
        public List<Integer> negativeDomain;
        public List<Integer> positiveDomain;
        public double load;

        public Agent(int pId, int pStartingLoc, double pload) {
            vehicleID = pId;
            route = new ArrayList<>();
            negativeDomain = new ArrayList<>();
            positiveDomain = new ArrayList<>();
            route.add(pStartingLoc);
            load = pload;
        }
    }

    //struct/class to hold onto a set variables that can be altered later for basic search/comparison
    //domain variable represent % of distanceMean for nearby location range
    //multi variable % of distanceMean used to add onto location costs when picking BestNext
    static class SearchVar {
        public double distanceMean;
        public double negativeDomain;
        public double positiveDomain;
        public double negativeMulti;
        public double positiveMulti;

        public SearchVar(DataModel pData) {
            GetDistanceMedian(pData);
            negativeDomain = 1;
            positiveDomain = .5;
            negativeMulti = .01;
            positiveMulti = .1;
        }


        public void GetDistanceMedian(DataModel pData) {
            double temp = 0;
            for (int i = 0; i < pData.distanceMatrix[0].length; i++) {
                temp += pData.distanceMatrix[0][i];
            }
            distanceMean = temp / pData.distanceMatrix[0].length;
        }
    }

//    //making a copy first backup
//    static class Route {
//        public int vehicleID;
//        public List<Integer> route;
//        public double routeCost;
//        public Route()
//        {
//            route = new ArrayList<>();
//        }
//        public Route(int pId, int pStartingLoc)
//        {
//            vehicleID = pId;
//            route.add(pStartingLoc);
//        }
//    }

//    public static List<Routes> VRP() {
//        List<Routes> RoutingManager = new ArrayList<>();
//        DataModel data = new DataModel();
//        //get the ideal best route fastest route considering packages total / car total
//        //vehicles must return to depot thus extra distance added at the end
//        double bestPath = 0;
//        //long to truncate round down
//        //should check load fits vehicleCap leaving it out for now
//        //while loop while there is still demands = 1 meaning parcels to deliver
//        int currentVehicle = 0;
//        int test = 3;
//        //Searching loop start
//        while (test > 0) {
//            //creating/initializing vars to be used for our search
//            Routes route = new Routes();
//            boolean found = false;
//            List<Integer> currentRoute = new ArrayList<>();
//            currentRoute.clear();
//            //starting point of Routes == depot
//            currentRoute.add(0);
//            List<Integer> locations = new ArrayList<>();
//            //get the locations with demands (visit once)
//            //data.demands index = location index, value = (0,1) for delivery required
//            //converting to locations.list values contains location index
//            for (int d = 0; d < data.demands.size(); d++) {
////                System.out.println("Add Loc:"+d + " demands:"+data.demands.get(d));
//                //for every
//                if (data.demands.get(d) == 1) {
//                    locations.add(d);
//                }
//            }
//            //loop to add routes while locations to delivery >0
//            while (!found && (locations.size() > 0)) {
//                //double to hold our cost
//                double currentCost = 0;
//                //determining amount of packages a car holds going from least necessary to most by using truncation math.floor
//                if (data.vehicleNumber * data.vehicleCapacities[0] < data.packages)
//                    System.out.println("Capacity < parcels to delivery in 1 run" + RoutingManager.size());
//                //int load = (int)Math.floor(parcels/data.vehicleNumber);
//                //using simple load=6 debugging
//                int load = data.vehicleCapacities[currentVehicle];
//                if (load > data.vehicleCapacities[currentVehicle])
//                    System.out.println("Load Error , above car capacity:" + load + data.vehicleCapacities[currentVehicle]);
//                //load is used to loop vehicle routing incase load>location size
//                if (locations.size() < load)
//                    load = locations.size();
//                //current base just gets the shortest route until capacity reached a multi travelling salesmen approach
//                //for testing
//                //loop to find a route for load number of times
//                for (int i = 0; i < load; i++) {
//                    int bestJ = 0;
//                    double bestCost = 999999;
//                    if (locations.size() > 0) {
//                        //get the lowest cost path form the last entry to currentRoute (current location)
//                        for (int j = 0; j < locations.size(); j++) {
//                            double current = data.distanceMatrix[currentRoute.get(currentRoute.size() - 1)][locations.get(j)];
//                            if (current < bestCost) {
//                                bestCost = current;
//                                bestJ = locations.get(j);
//                            }
//                        }
//                    }
//                    currentRoute.add(bestJ);
//                    currentCost += bestCost;
////                    System.out.println("Appending:"+bestJ+ " total:"+currentCost + " removing Loc:"+locations.indexOf(bestJ));
////                    System.out.println("CRsize:"+currentRoute.size() + " load:"+i);
//                    locations.remove(locations.indexOf(bestJ));
////                    System.out.println(locations.size()+"locSize");
//                }
//                //check that the search is complete
//                if (currentRoute.size() == load + 1 || currentRoute.size() == locations.size()) {
//                    //must return to depot so add this into routing and costs
//                    currentCost += data.distanceMatrix[currentRoute.get(currentRoute.size() - 1)][0];
//                    currentRoute.add(0);
////                    System.out.println("Loading data%ncar:"+currentVehicle+"%nRoute:"+currentRoute+ "%nCost:"+currentCost);
//                    //update Route class
//                    route.vehicleID = currentVehicle;
//                    route.routeCost = currentCost;
//                    route.route = currentRoute;
//                    //Update demands set demands index of route elements to 0
//                    for (int k = 0; k < currentRoute.size(); k++) {
////                        System.out.println("cRk:"+currentRoute.get(k)+ " demandsK:"+data.demands.get(currentRoute.get(k)));
//                        data.demands.set(currentRoute.get(k), 0);
//                    }
//                    //update vars for while(demands) loop
//                    if (currentVehicle < 3)
//                        currentVehicle++;
//                    //break route loop
//                    found = true;
//                }
//            }
//            RoutingManager.add(route);
//            //placeholder var to run our search loop 3 times for simplicity
//            test--;
//        }
//        return RoutingManager;
//    }

    //Neighbouring Location identifier
    public static List<Integer> NearbyDomain(double pRange, DataModel pData, List<Integer> pDomain, Integer pLoc) {
        List<Integer> nearbyDomain = new ArrayList<>();
        for (int i = 0; i < pDomain.size(); i++) {
            if (pData.distanceMatrix[pLoc][pDomain.get(i)] < pRange) {
                nearbyDomain.add(pDomain.get(i));
            }
        }
        return nearbyDomain;
    }

    //apply pVar to passed in test domain
    public static List<Integer> BestNext(List<Integer> pLoc, List<Integer> pDomain, DataModel pData, List<Agent> pAgents, SearchVar pSearchVar) {
        List<Integer> bestLocs = new ArrayList<>();
        List<Double> bestCosts = new ArrayList<>();
        //clear these 2 lists to be searched again when 1 location is selected
        bestLocs.clear();
        bestCosts.clear();
        //vehicleNum instead of 3 later
        for (int i = 0; i < pAgents.size(); i++) {
            //what needs to be found == returnLocs == null
            //vehicle index i == null. search for it
            int bestJ = 999999999;
            double bestCost = 999999999;
            //get the lowest cost path form the last entry to currentRoute (current location)
            //Weight constraint check to refine our domain space to pick
            List<Integer> domain = new ArrayList<>();
            domain.addAll(pDomain);
            //System.out.println("car:"+i+ " load:"+pAgents.get(i).load + "dom:"+domain);
            //use pDomain to cycle through and cut domain to avoid altering .size() causing errors
            for (int d = 0; d < pDomain.size(); d++) {
                //parcel weight in the domain > vehicle capacity
                if (pAgents.get(i).load < pData.parcelWeight[pDomain.get(d)]) {
                    //remove it from the search domain
                    //System.out.println("removing weight domain:"+ pDomain.get(d));
                    domain.remove(pDomain.get(d));
                }
            }
            for (int j = 0; j < domain.size(); j++) {
                //current loc of any driver should not be in pDomain as it is visited, besides 0
                double current = pData.distanceMatrix[pLoc.get(i)][domain.get(j)];
                //add in negativeDomain effects of vehicles other than i (current agent)
                for (int n = 0; n < pAgents.size(); n++) {
                    //not ur own negativeDomain
                    if (n != i) {
                        if (pAgents.get(n).negativeDomain.contains(domain.get(j))) {
                            //System.out.println("negativeDomain increase:" + n + " domJ:" + domain.get(j));
                            current += pSearchVar.negativeMulti * pSearchVar.distanceMean;
                        }
                    }
                }
                //positve domain incentive so we dont run into weight issues
                if (pAgents.get(i).positiveDomain.contains(domain.get(j))) {
                    current -= pSearchVar.positiveMulti * pSearchVar.distanceMean;
                }
                if (bestJ == 999999999) {
                    bestJ = domain.get(j);
                    bestCost = current;
                } else if (current < bestCost) {
                    bestJ = domain.get(j);
                    bestCost = current;
                }
            }
            //add i index bestJ and Cost as there wont always have 3 sizes to match index to vehicle numbers otherwise
            bestLocs.add(i, bestJ);
            bestCosts.add(i, bestCost);
        }
        return bestLocs;
    }

    public static void WeightConstraints(DataModel pData, List<Agent> pAgents, List<Integer> pDomain, SearchVar pSearchVar) {
        //this method is used to avoid running into not a final heavy parcel that no vehicle can manage.
        /*
        add parcels into agent's PositiveDomain to incentivise visiting those locations
        parcels whose weights * 3 > agent.load
         */
        for (Agent agent : pAgents) {
            List<Integer> priority = new ArrayList<>();
            agent.positiveDomain.clear();
            for (int i = 0; i < pDomain.size(); i++) {
                if ((pData.parcelWeight[pDomain.get(i)] * 3) >= agent.load) {
                    priority.add(pDomain.get(i));
                }
            }
            //if priority list>0 search taht they are close add to positive Domain of agent
            if (priority.size() > 0)
                agent.positiveDomain.addAll(NearbyDomain(pSearchVar.positiveDomain * pSearchVar.distanceMean, pData, priority, agent.route.get(agent.route.size() - 1)));
        }
    }

    public static void PickBestNext(List<Integer> pPaths, DataModel pData, List<Agent> pAgents) {
        int best = 999999999;
        double cost = 0;
        //pick the best next path that is the shortest distances, ignoring first locations which has all agents the same as we doa  different search for initial starting locations
        for (int i = 0; i < pPaths.size(); i++) {
            //99999999 is the default pPaths value of a path that has no possible outcomes from BestNext();
            if (pPaths.get(i) != 999999999)
            {
                if (best == 999999999)
                {
                    best = i;
                    cost = pData.distanceMatrix[pAgents.get(i).route.get(pAgents.get(i).route.size() - 1)][pPaths.get(i)];
                } else if (pData.distanceMatrix[pAgents.get(i).route.get(pAgents.get(i).route.size() - 1)][pPaths.get(i)] < cost) {
                    best = i;
                    cost = pData.distanceMatrix[pAgents.get(i).route.get(pAgents.get(i).route.size() - 1)][pPaths.get(i)];
                }
            }
        }
        //System.out.println("best:"+best+" : "+cost);
        if (best != 999999999) {
            //apply the best index route add routeCost first as it uses last route.
            pAgents.get(best).routeCost += pData.distanceMatrix[pAgents.get(best).route.get(pAgents.get(best).route.size() - 1)][pPaths.get(best)];
            pAgents.get(best).route.add(pPaths.get(best));
            //update demands lists in data
            pData.demands.set(pPaths.get(best), 0);
            pAgents.get(best).load -= pData.parcelWeight[pPaths.get(best)];
        }
        //System.out.println("paths:"+pPaths + " ="+best);
    }

//            //System.out.println("bestI:" + bestIndex + " bestCost:" + bestCost);
//            returnLocs.set(bestIndex, bestLocs.get(bestIndex));
//            pDomain.remove(bestLocs.get(bestIndex));
//            pData.demands.set(bestLocs.get(bestIndex),0);
//            //Update pLoc for negativeDomain
//            pLoc.set(bestIndex,bestLocs.get(bestIndex));
//            //update vehicle capacity -+ parcelWeight
//            pData.vehicleCapacities[bestIndex] -= pData.parcelWeight[bestLocs.get(bestIndex)];
//            //returnLocs contain 3 non null locations to return found = true
//            System.out.println("pDomSize:"+pDomain.size());
//            if (returnLocs.contains(null) != true || pDomain.size() == 0)
//                found = true;
//        }
//        return returnLocs;
//    }

    //find the bestPath assign to appropriate vehicles based on their position, if all same priotitize index 0->x
    //consideration to remove DataModel from param for performance.
    //pLoc = current vehicle locations, pDomain = nodes to visit/deliver
//    public static List<Integer> BestNext(List<Integer> pLoc, List<Integer> pDomain, DataModel pData) {
//        //index = vehicle id , value = new location
//        List<Integer> returnLocs = new ArrayList<>();
//        returnLocs.add(null);
//        returnLocs.add(null);
//        returnLocs.add(null);
//        List<Integer> bestLocs = new ArrayList<>();
//        List<Double> bestCosts = new ArrayList<>();
//        List<List<Integer>> negativeDomain = new ArrayList<>();
//        boolean found = false;
//        while (!found)
//        {
//            //clear these 2 lists to be searched again when 1 location is selected
//            negativeDomain.clear();
//            bestLocs.clear();
//            bestCosts.clear();
//            System.out.println("Clear");
//            //vehicleNum instead of 3 later
//            for (int i = 0; i < pLoc.size(); i++)
//            {
//                //generate negativeDomains for each vehicle
//                negativeDomain.add(i,NegativeDomain(400, pData, pDomain, pLoc.get(i)));
//                for(int d = 0; d<negativeDomain.size();d++) {
//                    System.out.println("nDom:"+d+" at:"+pLoc.get(d) + negativeDomain.get(d));
//                }
//                //what needs to be found == returnLocs == null
//                //vehicle index i == null. search for it
//                if (returnLocs.get(i) == null) {
//                    System.out.println(returnLocs.get(i) + " reloc:" + i);
//                    int bestJ = 0;
//                    double bestCost = 999999;
//                    //get the lowest cost path form the last entry to currentRoute (current location)
//                    //Weight constraint check to refine our domain space to pick
//                    List<Integer> domain = new ArrayList<>();
//                    domain.addAll(pDomain);
//                    for(int d = 0; d<domain.size();d++)
//                    {
//                        System.out.println("pDomain:"+pDomain.get(d));
//                        //parcel weight in the domain > vehicle capacity
//                        if(pData.vehicleCapacities[i] < pData.parcelWeight[domain.get(d)])
//                        {
//                            //remove it from the search domain
//                            System.out.println("load:"+pData.vehicleCapacities[i] +" parcelWeight:"+pData.parcelWeight[domain.get(d)]);
//                            System.out.println("removing domain:"+domain.get(d));
//                            domain.remove(d);
//                        }
//                    }
//                    for (int j = 0; j < domain.size(); j++) {
//                        //current loc of any driver should not be in pDomain as it is visited, besides 0
//                        double current = pData.distanceMatrix[pLoc.get(i)][domain.get(j)];
//                        //add in negativeDomain effects
//                        for (int n =0; n< negativeDomain.size();n++)
//                        {
//                            //not ur own negativeDomain
//                            if (n!=i)
//                            {
//                                if (negativeDomain.get(n).contains(domain.get(j)))
//                                {
//                                    System.out.println("negativeDomain increase:"+n+ " domJ:"+domain.get(j));
//                                    current += 100;
//                                }
//                            }
//                        }
//                        if (current < bestCost) {
//                            bestCost = current;
//                            bestJ = pDomain.get(j);
//                        }
//                    }
//                    //add i index bestJ and Cost as there wont always have 3 sizes to match index to vehicle numbers otherwise
//                    bestLocs.add(i, bestJ);
//                    bestCosts.add(i, bestCost);
//                }
//                // LocsReturn i has a path already set Locs/Costs i to null to ignore later
//                else
//                {
//                    bestLocs.add(i,null);
//                    bestCosts.add(i,null);
//                }
//            }
//            int bestIndex = 0;
//            double bestCost = 99999;
//            for (int i = 0; i < bestCosts.size(); i++) {
//                if (bestCosts.get(i) != null)
//                {
//                    if (bestCosts.get(i) < bestCost) {
//                        bestIndex = i;
//                        bestCost = bestCosts.get(i);
//                    }
//                }
//            }
//            //System.out.println("bestI:" + bestIndex + " bestCost:" + bestCost);
//            returnLocs.set(bestIndex, bestLocs.get(bestIndex));
//            pDomain.remove(bestLocs.get(bestIndex));
//            pData.demands.set(bestLocs.get(bestIndex),0);
//            //Update pLoc for negativeDomain
//            pLoc.set(bestIndex,bestLocs.get(bestIndex));
//            //update vehicle capacity -+ parcelWeight
//            pData.vehicleCapacities[bestIndex] -= pData.parcelWeight[bestLocs.get(bestIndex)];
//            //returnLocs contain 3 non null locations to return found = true
//            System.out.println("pDomSize:"+pDomain.size());
//            if (returnLocs.contains(null) != true || pDomain.size() == 0)
//                found = true;
//        }
//        return returnLocs;
//    }

    //refactoring code into method splits for DFS searching thus param missing p Casing for this method only
    public static List<Agent> SearchRoutes(DataModel data, SearchVar searchVar)
    {
        //initialize RouteManager List wiht 3 new Routes, add depot as first route location
        List<Agent> agentManager = new ArrayList<>();
        for (int i = 0; i < data.vehicleNumber; i++) {
            Agent temp = new Agent(i, data.depot, data.vehicleCapacities[i]);
            agentManager.add(temp);
        }
        //get demanding deliveries
        while (data.demands.contains(1)) {
            List<Integer> currentLoc = new ArrayList<>();
            List<Integer> domain = new ArrayList<>();
            for (int i = 0; i < data.demands.size(); i++) {
                if (data.demands.get(i) == 1) {
                    //System.out.println("demands:"+i);
                    domain.add(i);
                }
            }
            //populate PositiveDomain when weight becomes scarce
            WeightConstraints(data, agentManager, domain, searchVar);
            for (int i = 0; i < agentManager.size(); i++) {
                //saves repeating this line in searches
                currentLoc.add(agentManager.get(i).route.get(agentManager.get(i).route.size() - 1));
                agentManager.get(i).negativeDomain.clear();
                agentManager.get(i).negativeDomain.addAll(NearbyDomain(searchVar.negativeDomain * searchVar.distanceMean, data, domain, agentManager.get(i).route.get(agentManager.get(i).route.size() - 1)));
                //System.out.println("agent" + i + ":Neg" + agentManager.get(i).negativeDomain + "Pos:" + agentManager.get(i).positiveDomain + " :Route " + agentManager.get(i).route);
            }
            List<Integer> test = BestNext(currentLoc, domain, data, agentManager, searchVar);
            int failedsearch = 0;
            for(Integer i : test)
            {
                if(i == 999999999)
                {
                    failedsearch++;
                }
            }
            if (failedsearch == test.size())
            {
                break;
            }
            PickBestNext(test, data, agentManager);
        }
        //add the final destination + cost back to warehouse
        for (int i = 0; i < agentManager.size(); i++) {

            UpdateAgents(agentManager, i, 0, data);
            //System.out.println("return AgentManager:"+agentManager.get(i).route);
        }
        return agentManager;
    }

    public static void EvaluateSearch(List<Agent> pAgents, SearchVar pSearchVar, DataModel pData)
    {
        //do some evaluation of the search results and alter psearchVar for stricter weight/no nearby vehicles.
        /*
        Lots of free space/load on vehicles reduce priority on heavy parcels
         */
        //search only fails when left over locations weight > individual vehicle's load but not load total
        if (pData.demands.contains(1))
        {
            if (pSearchVar.positiveMulti < 1)
            {
                pSearchVar.positiveMulti += .1;
                System.out.println("Failed search increasing positiveMulti:"+pSearchVar.positiveMulti);
            }
            else if(pSearchVar.positiveMulti >= 1)
            {
                if(pSearchVar.positiveDomain <= 2)
                {
                    pSearchVar.positiveDomain += .1;
                    System.out.println("Failed search increasing positiveDom:"+pSearchVar.positiveDomain);
                }
            }
        }
        //completed search play with changes to spread of vehicles
        else if(!pData.demands.contains(1))
        {
            if (pSearchVar.negativeMulti < 1)
            {
                pSearchVar.negativeMulti += .1;
                System.out.println("Altering search increasing negativeMulti:"+pSearchVar.negativeMulti);
            }
        }

    }

    public static List<List<Integer>> VehicleRouting() {
        DataModel data = new DataModel();
        SearchVar searchVar = new SearchVar(data);
        List<List<Agent>> allAgents = new ArrayList<>();
        while(allAgents.size() < 10)
        {
            List<Agent> agentManager = SearchRoutes(data, searchVar);
            //evaluate search variables alter searchVar
            EvaluateSearch(agentManager,searchVar, data);
            //successful search
            if(!data.demands.contains(1))
            {
                allAgents.add(agentManager);
            }
            //reset dataModel for data.demands
            data = new DataModel();
        }
        double bestAverage = 999999999;
        Integer bestIndex = 999999999;
        for (int i = 0; i < allAgents.size(); i++) {
            double average = 0;
            for (int j = 0; j < allAgents.get(i).size();j++)
            {
                average += allAgents.get(i).get(j).routeCost;
            }
            if (bestIndex==999999999)
            {
                bestIndex = i;
                bestAverage = average/allAgents.get(i).size();
            }
            else if(average/allAgents.get(i).size() < bestAverage)
            {
                bestAverage = average/allAgents.get(i).size();
                bestIndex = i;
            }
        }
        List<List<Integer>> routes = new ArrayList<>();
        if (bestIndex != 999999999)
        {
            for (int i =0; i < allAgents.get(bestIndex).size();i++)
            {
                routes.add(allAgents.get(bestIndex).get(i).route);
            }
            System.out.println("avg:"+bestAverage);
        }
        return routes;
    }

    //adds routeCost from last to new loc, add route(pNewLoc)
    public static void UpdateAgents(List<Agent> pRM, Integer pI, Integer pNewLoc, DataModel pData) {
        //update routeCost from current last routeindex to pNewLoc
        int temp = pRM.get(pI).route.size() - 1;
        //System.out.println(temp + pNewLoc);
        pRM.get(pI).routeCost += pData.distanceMatrix[temp][pNewLoc];
        pRM.get(pI).route.add(pNewLoc);
    }


    public static void main(String[] args) {
        List<List<Integer>> routes = VehicleRouting();
        for (int i = 0; i < routes.size(); i++) {
            System.out.println("Results:"+i+routes.get(i));
        }
    }
}