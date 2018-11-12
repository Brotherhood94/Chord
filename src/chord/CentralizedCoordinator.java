package chord;


import java.math.BigInteger;
import java.util.*;
import java.util.Random;


public class CentralizedCoordinator {
    private Random random = new Random();

    private TreeMap<BigInteger, FingerTable> fingerTables;      //For simplicity I'm assuming that the number of peers is less than 2^32 otherwise I should use Big Number Data Structure.
    private ArrayList<BigInteger> nodes;
    private InfoLogger infoLogger = null;


    //idBits = number of bits of the identifiers of a Chord ring
    //nodesNumber = number of peers
    public CentralizedCoordinator(int idBits, int nodesNumber, int queries, String path) {
        FingerTable.setIdBits(idBits);      //I'm already setting the static variable idBits of FingerTable class. It's static because each finger table has to have maximum idBits. --> idBits does not dipend on the fingertable instance, it is logically static.
        Utility utility = new Utility(idBits);
        //Generating nodes
        this.nodes = generateNodes(nodesNumber, utility);
        //Setting up the logger
        this.infoLogger = new InfoLogger(path, nodes, idBits, queries);
        //Settting up fingertables
        this.fingerTables = setUpDataStructure(nodes);
        //Generating keys
        ArrayList<BigInteger> keys = generateKeysToQuery(queries, random, utility);
        //Starting the look up
        this.lookUp(keys);
        //Save logged infos
        this.infoLogger.saveInfos(this.fingerTables);
    }


    private static ArrayList<BigInteger> generateNodes(int nodesNumber, Utility utility){
        BigInteger idNode = null;
        ArrayList<BigInteger> out = new ArrayList<>();
        for(int i = 0; i < nodesNumber; i++){
            do{
                idNode = utility.getSHA1Node(utility.getRandomIp(), utility.getRandomPort()); //In that way, I guarantee that each Node has a different ID
            }
            while (out.contains(idNode));
            out.add(idNode);
        }
        Collections.sort(out);
        return out;
    }

    private static TreeMap<BigInteger, FingerTable> setUpDataStructure(ArrayList<BigInteger> nodes){
        FingerTable ft;
        BigInteger idNode, predecessor;
        TreeMap<BigInteger, FingerTable> out = new TreeMap<>();
        for(int i = 0; i < nodes.size(); i++) {
            idNode = nodes.get(i);
            try {
                predecessor = nodes.get(i-1);
            }catch (IndexOutOfBoundsException e){
                predecessor = nodes.get(nodes.size()-1);        //case in which idNode < predecessor. E.g: idNode is the first node in the ring.
            }
            out.put(idNode, ft = new FingerTable(idNode, predecessor, nodes)); //Creating FingerTable for idNode
            ft.printFingerTable();
        }
        return out;
    }

    private static ArrayList<BigInteger> generateKeysToQuery(int queries, Random random, Utility utility){
        ArrayList<BigInteger> keys = new ArrayList<>();
        for(int i = 0; i < queries; i++)
            keys.add(utility.getSHA1Key(String.valueOf(random.nextInt()))); //Generating random keys
        return keys;
    }

    private void lookUp(ArrayList<BigInteger> keys){
        for(BigInteger key : keys){
            infoLogger.newRoute();
            BigInteger startNode = nodes.get(random.nextInt(nodes.size())); //Chosing a random peer among those generated //I'm assuming that peers are less than 2^32. That's because otherwise I could not use HashMap. (Docs: Hashmap.size() returns an int (= 2^32))
            this.findSuccessor(startNode, key, infoLogger);
        }
    }

    private BigInteger findSuccessor(BigInteger node, BigInteger key, InfoLogger infoLogger){
        FingerTable fingerTable = fingerTables.get(node);
        BigInteger predecessor = fingerTable.getPredecessor();
        BigInteger successor = fingerTable.getEntryLink(0);
        infoLogger.setInfo(node);
        if( predecessor != null && interval(predecessor, node, key))
            return node;
        else if( interval(node, successor, key) )
            return successor;
        else {
            BigInteger m = closestPrecedingNode(fingerTable, node, key);
            return findSuccessor(m, key, infoLogger);
        }
    }
    
    private BigInteger closestPrecedingNode(FingerTable ft, BigInteger node, BigInteger key){
        for(int i = ft.getSize()-1; i >= 0; i--)
            if(interval2(node, key, ft.getEntryLink(i)))
                return ft.getEntryLink(i);
        return node;
    }

    private static boolean interval(BigInteger a, BigInteger b, BigInteger key){ //b is included in the interval
        if( a.compareTo(b) < 0 )
            return (key.compareTo(a) > 0 && key.compareTo(b) <= 0);
        return ( key.compareTo(a) > 0 || key.compareTo(b) <= 0 );
    }

    private static boolean interval2(BigInteger a, BigInteger b, BigInteger key){ //b is NOT included in the interval
        if( a.compareTo(b) < 0 )
            return (key.compareTo(a) > 0 && key.compareTo(b) < 0);
        return (  key.compareTo(a) > 0 || key.compareTo(b) < 0 );
    }
}
