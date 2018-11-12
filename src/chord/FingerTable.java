package chord;


import java.math.BigInteger;
import java.util.*;

public class FingerTable{

    private static int idBits;
    private List<Entry> entries; //Number of entries is equal to idBits value
    private BigInteger idNode = null;
    private BigInteger predecessor = null;
    public FingerTable(BigInteger idNode, BigInteger predecessor, ArrayList<BigInteger> nodes) {
        this.idNode = idNode;
        this.predecessor = predecessor;
        this.entries = setUpFingerTable(idNode, nodes);
    }

    public BigInteger getEntryTarget(int index) {
        BigInteger target = null;
        try {
            target =  this.entries.get(index).getTarget();
        }catch (IndexOutOfBoundsException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return target;
    }

    public BigInteger getEntryLink(int index) {
        BigInteger link = null;
        try {
            link = this.entries.get(index).getLink();
        }catch (IndexOutOfBoundsException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return link;
    }

    public BigInteger getPredecessor(){
        return this.predecessor;
    }

    public int getSize(){
        return this.entries.size();
    }

    public static void setIdBits(int idBits){
        FingerTable.idBits = idBits;
    }

    private static List<Entry> setUpFingerTable(BigInteger idNode, ArrayList<BigInteger> nodes){
        BigInteger target, link;
        List<Entry> out = Arrays.asList(new Entry[idBits]);    //It's programatically guarantees that the list cannot have more than idBits position  //Syntactic sugar
        for(int i = 0; i < out.size(); i++){
            target = (idNode.add(BigInteger.valueOf(2).pow(i))).mod(BigInteger.valueOf(2).pow(idBits)); //The formula is (n + 2^(i-1) but since the array is indexed from 0, I can simply compute (n +2^i)
            link = setLink(target, nodes);
            out.set(i, new Entry(target, link));
        }
        return out;
    }

    private static BigInteger setLink(BigInteger target, ArrayList<BigInteger> nodes){
        int i = 0;
        try {
            while (target.compareTo(nodes.get(i)) > 0)
                i++;
        }catch(IndexOutOfBoundsException e){        //Case in which the target is greater than the last peer. In this case, it means that the target belongs to the first node in the ring.  //Needed due to the circulr-array-style of Chord
            i = 0;
        }
        return nodes.get(i);    //This is the first node clockwise starting from the target
    }

    public void printFingerTable(){
        System.out.println("\nFingerTable\nNodeID: "+this.idNode.toString()+"  ------------ Predecessor: "+this.predecessor.toString());
        System.out.println("i target link");
        for(int i = 0; i < entries.size(); i++)
            System.out.println((i+1)+" "+entries.get(i).getTarget().toString()+" "+entries.get(i).getLink().toString());
        System.out.println("------------\n");
    }
    
    private static class Entry{     //This is an inner private class because logically it can be access only by the FingerTable class (encapsulation). //Syntactic sugar
        private BigInteger target;
        private BigInteger link;

        private Entry(BigInteger target, BigInteger link) {
            this.target = target;
            this.link = link;
        }

        private BigInteger getTarget(){
            return this.target;
        }

        private BigInteger getLink(){
            return this.link;
        }
    }

}
