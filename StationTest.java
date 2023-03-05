import java.util.*;

public class StationTest {
    public static void main(String[] args) {
        Station txStation = new Station(3,10,0);
        Station rxStation = new Station(3,10,0);


        System.out.printf("isReady()=%b\n",txStation.isReady());
       // System.out.printf("Sending 3 frames with the payloads a,b,c dump send buf\n");
     txStation.send(0xa);
    
    txStation.send(0xb);
    txStation.send(0xc);
    // should see the 3 frames in sbuf
    txStation.printbuf(txStation.sbuf);
    
  
        byte[] txframe = txStation.nextTransmitFrame() ;
       System.out.printf("nextTransitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
         txframe = txStation.nextTransmitFrame() ;
       System.out.printf("nextTransitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
       txframe = txStation.nextTransmitFrame() ;
       System.out.printf("nextTransitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
       txframe = txStation.nextTransmitFrame() ;
       System.out.printf("nextTransitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
       txframe = txStation.nextTransmitFrame() ;
       System.out.printf("nextTransitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
       


       
       
       //rxStation.receiveFrame(txframe);

    }
}
