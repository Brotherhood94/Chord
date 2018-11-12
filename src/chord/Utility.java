package chord;

import java.math.BigInteger;
import java.security.*;
import java.util.Random;

public class Utility {
    private int idBits;
    private Random random;

    public Utility(int idBits){
        this.idBits = idBits;
        this.random = new Random();
    }


    public BigInteger getSHA1Node(String ip, String port){
        BigInteger idNode = null;
        MessageDigest md = null;
        String input = ip+":"+port;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            return null;
        }
        idNode = new BigInteger(md.digest(input.getBytes())).mod(BigInteger.valueOf(2).pow(idBits));     //SHA("ip:port") = out, where out is 160 bit. So I use module function to be able to map the idNode on the Chord ring, in the case in which, idBits < 160
        System.out.println(input+" --> idNode: "+idNode.toString());
        return idNode;
    }

    public BigInteger getSHA1Key(String data){
        BigInteger key = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            return null;
        }
        key =  new BigInteger(md.digest(data.getBytes())).mod(BigInteger.valueOf(2).pow(idBits));      //SHA("ip:port") = out, where out is 160 bit. So I use module function to be able to map the idNode on the Chord ring, in the case in which, idBits < 160
        return key;
    }


    public String getRandomPort(){
        int port = random.nextInt(65536);
        return String.valueOf(port);
    }

    public String getRandomIp(){
        return random.nextInt(255)+"."+random.nextInt(255)+"."+random.nextInt(255)+"."+random.nextInt(255);
    }
}
