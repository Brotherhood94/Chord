import chord.CentralizedCoordinator;

import java.math.BigInteger;


public class Main {

    public static void main(String[] args) {
        int idBits = -1, nodesNumber = -1, queries = -1;
        String path = null;

        try {
            idBits = Integer.valueOf(args[0]);
            nodesNumber = Integer.valueOf(args[1]);
            queries = Integer.valueOf(args[2]);
            path = args[3];
        }catch(NumberFormatException e){
            System.err.println("Inputs must be integers. Retry.");
            System.exit(1);
        }
        BigInteger idSpace = BigInteger.valueOf(2).pow(idBits);
        if(idSpace.compareTo(new BigInteger(args[1])) < 0) {
            System.err.println("The number of peers cannot be higher than the cardinality of the ids space");
            return;
        }
        new CentralizedCoordinator(idBits, nodesNumber, queries, path);

    }

}
