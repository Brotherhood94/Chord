package chord;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class InfoLogger {
    private int hopsCounter = 0;
    private HashMap<Integer,Integer> hopsFrequency; //maps a fixed number of hop to the times that a key need this fixed number of hop to be resolved
    private ArrayList<Integer> queriesHops; //list of hops needed to resolve each query
    private TreeMap<BigInteger, Integer> queriesSeen;   //Maps each nodeID to a number that represents the total number of queries forwarded by him
    private int idBits, peers, queries;
    private String idSimulation = null, path = null;
    private String info;
    public InfoLogger(String path, ArrayList<BigInteger> nodes, int idBits, int queries) {
        this.idBits = idBits;
        this.peers = nodes.size();
        this.queries = queries;
        this.info = "Bits: " + this.idBits + "  |  Peers: " + this.peers + "  |  Queries: " + this.queries;
        this.hopsFrequency = new HashMap<>();
        this.queriesHops = new ArrayList<>();
        this.queriesSeen = initializeQueriesSeen(nodes);
        this.idSimulation = +idBits+"-"+peers+"-"+queries;
        this.path = generateDirectory(path, idSimulation);
    }

    private TreeMap<BigInteger, Integer> initializeQueriesSeen(ArrayList<BigInteger> nodes){
        TreeMap<BigInteger, Integer> out = new TreeMap<>();
        for(BigInteger node : nodes)
            out.put(node, 0);
        return out;
    }

    public void setInfo(BigInteger node){
        this.hopsCounter++;
        this.incrementNumberQuerySeen(node);
    }

    public void newRoute(){
        Integer oldValue = hopsFrequency.get(hopsCounter);
        if(oldValue == null)
            hopsFrequency.put(hopsCounter, 1);
        else
            hopsFrequency.put(hopsCounter, ++oldValue);
        this.queriesHops.add(hopsCounter);
        this.hopsCounter = -1;
    }

    private void incrementNumberQuerySeen(BigInteger node){
        Integer oldValue = queriesSeen.get(node);
        this.queriesSeen.replace(node, ++oldValue);
    }

    public void saveInfos(TreeMap<BigInteger, FingerTable> fingerTables){
        System.out.println("Saving Results: the amount of time depends on the idBits/queries setted in input.");
        this.saveStatisticalChart();
        this.saveTopologicalChart(fingerTables);
        this.saveFingerTables(fingerTables);
        this.saveGraph(fingerTables);
        System.out.println("Finished.");
    }

    private String generateDirectory(String path, String idSimulation){
        File file = new File(path+"/"+idSimulation);
        if(!file.exists())
            file.mkdirs();
        return file.getAbsolutePath();
    }

    private void saveStatisticalChart(){
        Chart routes, seen, hopsFrequency;

        //Hop Frequency Chart
        String chartHopsTitle = "Hop Frequency\n"+info;
        hopsFrequency = new Chart(chartHopsTitle, "Hop", "Frequency",  Color.blue);
        for(Map.Entry<Integer,Integer> entry : this.hopsFrequency.entrySet())
            hopsFrequency.addToDataset(entry.getValue(), "routeHop", String.valueOf(entry.getKey()));
        hopsFrequency.saveChart(this.path+"/HopsFrequency-"+idSimulation+".png");
        System.out.println("Saving Statistical Chart [1/3] ...");

        //Hop for each Query Chart
        String chartRoutesTitle = "Hop for each Query\n"+info+"\nN° Hops with High Probability: "+String.format("%.2f", Math.log(this.peers) / Math.log(2))+"\n"; //log base 2 of peers =  Math.log(this.peers) / Math.log(2)
        routes = new Chart(chartRoutesTitle, "Routes", "Hop",  Color.blue);
        int sum = 0;
        for(int i = 0; i < this.queriesHops.size(); i++) {
            routes.addToDataset(this.queriesHops.get(i), "routeHop", String.valueOf(i));
            sum+=this.queriesHops.get(i);
        }
        double mean = (double) sum/this.queriesHops.size();
        routes.addMarker(mean, "Avg. Number of Routing Hops", Color.green);
        routes.addMarker(Math.log(this.peers) / Math.log(2), "Hops High Probability", Color.red);
        routes.saveChart(this.path+"/HopForEachKey-"+idSimulation+".png");
        System.out.println("Saving Statistical Chart [2/3]...");

        //Queries Seen By each Node Chart
        String chartSeenTitle = "Queries Seen By each Node\n "+info;
        seen = new Chart(chartSeenTitle, "NodeID", "Frequency",  Color.blue);
        for(Map.Entry<BigInteger, Integer> entry : this.queriesSeen.entrySet())
            seen.addToDataset(entry.getValue(), "Frequency", entry.getKey().toString());
        seen.saveChart(this.path+"/QuerySeenByANode-"+idSimulation+".png");
        System.out.println("Saving Statistical Chart [3/3]...");
    }

    private void saveTopologicalChart(TreeMap<BigInteger, FingerTable> fingerTables){
        HashSet<BigInteger> set;
        HashMap<BigInteger, InOut> edges = new HashMap<>();
        InOut inOut;
        for(Map.Entry<BigInteger, FingerTable> entry : fingerTables.entrySet()) {
            BigInteger nodeID = entry.getKey();
            FingerTable ft = entry.getValue();
            set = new HashSet<>();
            for (int i = 0; i < ft.getSize(); i++)
                set.add(ft.getEntryLink(i));
            if( (inOut = edges.get(nodeID)) != null )
                inOut.setOutEdges(set.size());
            else
                edges.put(nodeID, new InOut(set.size()));
            for(BigInteger node : set) {
                if ( (inOut = edges.get(node)) != null)
                    inOut.incInEdge();
                else
                    edges.put(node, new InOut());
            }
        }

        Integer oldValue;
        TreeMap<Integer, Integer> inCounter, outCounter;
        Chart inEdges, outEdges;
        inCounter = new TreeMap<>();
        outCounter= new TreeMap<>();
        for(Map.Entry<BigInteger, InOut> entry : edges.entrySet()){
            Integer inSize = entry.getValue().getIn();
            Integer outSize = entry.getValue().getOut();
            if( (oldValue = inCounter.get(inSize)) != null)
                inCounter.replace(inSize, ++oldValue);
            else
                inCounter.put(inSize, 1);

            if( (oldValue = outCounter.get(outSize)) != null)
                outCounter.replace(outSize, ++oldValue);
            else
                outCounter.put(outSize, 1);
        }

        String inEdgeTitle = "In Edges Frequency\n"+info;
        inEdges = new Chart(inEdgeTitle, "InDegree", "Frequency", Color.red);
        for(Map.Entry<Integer, Integer> entry : inCounter.entrySet())
            inEdges.addToDataset(entry.getValue(), "inDegree", String.valueOf(entry.getKey()));
        inEdges.saveChart(this.path+"/InDegreeFrequency-"+idSimulation+".png");
        System.out.println("Saving Topological Chart [1/2] ...");


        String outEdgeTitle = "Out Edges Frequency\n"+info;
        outEdges = new Chart(outEdgeTitle, "OutDegree", "Frequency", Color.red);
        for(Map.Entry<Integer, Integer> entry : outCounter.entrySet())
            outEdges.addToDataset(entry.getValue(), "outDegree", String.valueOf(entry.getKey()));
        outEdges.saveChart(this.path+"/OutDegreeFrequency-"+idSimulation+".png");
        System.out.println("Saving Topological Chart [2/2] ...");
    }

    private void saveGraph(TreeMap<BigInteger, FingerTable> fingerTables){
        HashSet<BigInteger> set;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.path+"/ChordGraphs"+idSimulation+".csv"))) {
            bw.write("Source,Target\n");
            for(Map.Entry<BigInteger, FingerTable> entry : fingerTables.entrySet()){
                BigInteger nodeID = entry.getKey();
                FingerTable ft = entry.getValue();
                set = new HashSet<>();
                for(int i = 0; i < ft.getSize(); i++)
                    set.add(ft.getEntryLink(i));
                for(BigInteger dest : set)
                    bw.write(nodeID + "," + dest + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFingerTables(TreeMap<BigInteger, FingerTable> fingerTables){
        try (BufferedWriter  bw = new BufferedWriter(new FileWriter(path+"/fingertables"+idSimulation+".csv"))) {
            for(Map.Entry<BigInteger, FingerTable> entry : fingerTables.entrySet()){
                bw.write(entry.getKey()+";");
                FingerTable fingerTable = entry.getValue();
                for(int i = 0; i < fingerTable.getSize(); i++)
                    bw.write(fingerTable.getEntryTarget(i)+","+fingerTable.getEntryLink(i)+";");
                bw.write("\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static class InOut{
        private Integer in;  //N° in edges
        private Integer out; //N° out edges
        protected InOut(Integer out){
            this.out = out;
            this.in = 0;
        }

        protected InOut(){
            this.out = 0;
            this.in = 1;
        }

        protected void setOutEdges(Integer out){
            this.out = out;
        }

        protected void incInEdge(){
            this.in+=1;
        }

        protected Integer getIn(){
            return this.in;
        }

        protected Integer getOut(){
            return this.out;
        }

    }

}
