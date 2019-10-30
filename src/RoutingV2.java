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

public class RoutingV2 {
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
        public boolean reverse;

        public SearchVar(DataModel pData) {
            GetDistanceMedian(pData);
            negativeDomain = .1;
            positiveDomain = .5;
            negativeMulti = .5;
            positiveMulti = .2;
            reverse = false;
        }


        public void GetDistanceMedian(DataModel pData) {
            double temp = 0;
            for (int i = 0; i < pData.getDistanceMatrixSize(); i++) {
                temp += pData.getDistance(0, i);
            }
            distanceMean = temp / pData.getDistanceMatrixSize();
        }
    }

    //Neighbouring Location identifier
    public static List<Integer> NearbyDomain(double pRange, DataModel pData, List<Integer> pDomain, Integer pLoc) {
        List<Integer> nearbyDomain = new ArrayList<>();
        for (int i = 0; i < pDomain.size(); i++) {
            if (pData.getDistance(pLoc, pDomain.get(i)) < pRange) {
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
            //use pDomain to cycle through and cut domain to avoid altering .size() causing errors
            for (int d = 0; d < pDomain.size(); d++) {
                //parcel weight in the domain > vehicle capacity
                if (pAgents.get(i).load < pData.getParcelWeight(pDomain.get(d))) {
                    //remove it from the search domain
                    domain.remove(pDomain.get(d));
                }
            }
            for (int j = 0; j < domain.size(); j++) {
                //current loc of any driver should not be in pDomain as it is visited, besides 0
                double current = pData.getDistance(pLoc.get(i), domain.get(j));
                //add in negativeDomain effects of vehicles other than i (current agent)
                for (int n = 0; n < pAgents.size(); n++) {
                    //not ur own negativeDomain
                    if (n != i) {
                        if (pAgents.get(n).negativeDomain.contains(domain.get(j))) {
                            //Formula used 1 - nearbyVehicleDistanceToLoc / DistanceMean gets our % that is high when distance is close for deterrence and low when its further for encouraging incentive
                            double nearbyVehicleDistance = pData.getDistance(pLoc.get(n), domain.get(j));
                            double nearbyVehicleMulti = 1 - (nearbyVehicleDistance/pSearchVar.distanceMean);
                            current += pSearchVar.negativeMulti * pSearchVar.distanceMean *nearbyVehicleMulti;
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
                if ((pData.getParcelWeight(pDomain.get(i)) * 3) >= agent.load) {
                    priority.add(pDomain.get(i));
                }
            }
            //if priority list>0 search that they are close add to positive Domain of agent
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
                    cost = pData.getDistance(pAgents.get(i).route.get(pAgents.get(i).route.size() - 1), pPaths.get(i));
                } else if (pData.getDistance(pAgents.get(i).route.get(pAgents.get(i).route.size() - 1), pPaths.get(i)) < cost) {
                    best = i;
                    cost = pData.getDistance(pAgents.get(i).route.get(pAgents.get(i).route.size() - 1), pPaths.get(i));
                }
            }
        }
        if (best != 999999999) {
            //apply the best index route add routeCost first as it uses last route.
            pAgents.get(best).routeCost += pData.getDistance(pAgents.get(best).route.get(pAgents.get(best).route.size() - 1), pPaths.get(best));
            pAgents.get(best).route.add(pPaths.get(best));
            //update demands lists in data
            pData.getDemands().set(pPaths.get(best), 0);
            pAgents.get(best).load -= pData.getParcelWeight(pPaths.get(best));
        }
    }

    //refactoring code into method splits for DFS searching thus param missing p Casing for this method only
    public static List<Agent> SearchRoutes(DataModel data, SearchVar searchVar)
    {
        //initialize RouteManager List wiht 3 new Routes, add depot as first route location
        List<Agent> agentManager = new ArrayList<>();
        for (int i = 0; i < data.getNumberOfVehicles(); i++) {
            Agent temp = new Agent(i, data.getDepot(), data.getVehicleCapacity(i));
            agentManager.add(temp);
        }
        //get demanding deliveries
        while (data.getDemands().contains(1)) {
            List<Integer> currentLoc = new ArrayList<>();
            List<Integer> domain = new ArrayList<>();
            for (int i = 0; i < data.getDemands().size(); i++) {
                if (data.getDemands().get(i) == 1) {
                    domain.add(i);
                }
            }
            //populate PositiveDomain when weight becomes scarce
            WeightConstraints(data, agentManager, domain, searchVar);
            for (int i = 0; i < agentManager.size(); i++) {
                //saves repeating this line in searches
                currentLoc.add(agentManager.get(i).route.get(agentManager.get(i).route.size() - 1));
                //clear and re define negativeDomains for each agent
                agentManager.get(i).negativeDomain.clear();
                agentManager.get(i).negativeDomain.addAll(NearbyDomain(searchVar.negativeDomain * searchVar.distanceMean, data, domain, agentManager.get(i).route.get(agentManager.get(i).route.size() - 1)));
            }
            List<Integer> test = BestNext(currentLoc, domain, data, agentManager, searchVar);
            //this is a checker to break out of our search loop with an incomplete set of Routes as vehicle Load can not handle the left over parcels by themselves.
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
        to be continued to reverse step through each variable to test both ways for each set of variables.
        for higher accuracy.
         */
        //search only fails when left over locations weight > individual vehicle's load but not load total
        if (pData.getDemands().contains(1))
        {
            if (pSearchVar.positiveMulti < 2)
            {
                pSearchVar.positiveMulti += .1;
            }
            else if(pSearchVar.positiveMulti >= 2)
            {
                if(pSearchVar.positiveDomain <= 2.5)
                {
                    pSearchVar.positiveDomain += .1;
                    //reset positiveMulti to cycle searches again with increased range
                    pSearchVar.positiveMulti = .1;
                }
            }
        }
        //completed search play with changes to spread of vehicles
        else if(!pData.getDemands().contains(1))
        {
            //reset positiveMulti/Domain
            pSearchVar.positiveDomain = .5;
            pSearchVar.positiveMulti = .3;
            //search by increasing deterrence and incentive values first
            if (!pSearchVar.reverse)
            {
                if (pSearchVar.negativeMulti < 5)
                {
                    pSearchVar.negativeMulti += .25;
                }
                else if (pSearchVar.negativeMulti >=5)
                {
                    if(pSearchVar.negativeDomain <= 2.5)
                    {
                        pSearchVar.negativeDomain += .1;
                        //reset negativeMulti
                        pSearchVar.negativeMulti = .5;
                    }
                }
                if(pSearchVar.negativeMulti >= 5 && pSearchVar.negativeDomain>=2.5)
                {
                    //reverse search and reset the values
                    pSearchVar.reverse = true;
                    pSearchVar.negativeMulti = .5;
                    pSearchVar.negativeDomain = .1;
                }
            }
            //search going from increasing Radius first
            else if(pSearchVar.reverse)
            {
                if (pSearchVar.negativeDomain < 2.5)
                {
                    pSearchVar.negativeDomain += .1;
                }
                else if (pSearchVar.negativeDomain >=2.5)
                {
                    if(pSearchVar.negativeMulti <= 5)
                    {
                        pSearchVar.negativeMulti += .25;
                        pSearchVar.negativeDomain = .1;
                    }
                }
            }
        }

    }

    public static List<List<Integer>> VehicleRouting(DataModel pData) {
        DataModel data = pData;
        SearchVar searchVar = new SearchVar(data);
        List<List<Agent>> allAgents = new ArrayList<>();
        while(allAgents.size() < 250)
        {
            List<Agent> agentManager = SearchRoutes(data, searchVar);
            //evaluate search variables alter searchVar
            EvaluateSearch(agentManager,searchVar, data);
            //successful search
            if(!data.getDemands().contains(1))
            {
                allAgents.add(agentManager);
            }
            //reset dataModel for data.demands
            data.resetDemands();
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
        }
        return routes;
    }

    //adds routeCost from last to new loc, add route(pNewLoc)
    public static void UpdateAgents(List<Agent> pRM, Integer pI, Integer pNewLoc, DataModel pData) {
        //update routeCost from current last routeindex to pNewLoc
        int temp = pRM.get(pI).route.size() - 1;
        pRM.get(pI).routeCost += pData.getDistance(temp, pNewLoc);
        pRM.get(pI).route.add(pNewLoc);
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
        List<List<Integer>> routes = VehicleRouting(data);
        for (int i = 0; i < routes.size(); i++) {
            System.out.println("Results:"+i+routes.get(i));
        }
    }
}