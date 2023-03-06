import java.util.*;

public class StationTest {
    public static void main(String[] args) {
        Station txStation = new Station(5,3,(float)0);
        Station rxStation = new Station(5,3,(float)0);


        System.out.printf("isReady()=%b\n",txStation.isReady());
       // System.out.printf("Sending 3 frames with the payloads a,b,c dump send buf\n");
     
       txStation.send(0xa);
       txStation.send(0xb);
   
    
        txStation.send(0xc);

    
  
    byte[] txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
       rxStation.receiveFrame(txframe);
           byte [] rxframe = rxStation.nextTransmitFrame();
       System.out.printf("rxstation.nextTransmitFrame = %x %x %x %x %x\n",rxframe[0],rxframe[1],rxframe[2],rxframe[3],rxframe[4]);
       txStation.receiveFrame(rxframe);

        txframe = txStation.nextTransmitFrame();
       System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
         rxStation.receiveFrame(txframe);
              rxframe = rxStation.nextTransmitFrame();
         System.out.printf("rxstation.nextTransmitFrame = %x %x %x %x %x\n",rxframe[0],rxframe[1],rxframe[2],rxframe[3],rxframe[4]);
         txStation.receiveFrame(rxframe);
                                               
         txframe = txStation.nextTransmitFrame();
         System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
           rxStation.receiveFrame(txframe);
                rxframe = rxStation.nextTransmitFrame();
           System.out.printf("rxstation.nextTransmitFrame = %x %x %x %x %x\n",rxframe[0],rxframe[1],rxframe[2],rxframe[3],rxframe[4]);
           txStation.receiveFrame(rxframe);
           txframe = txStation.nextTransmitFrame();
           System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
             rxStation.receiveFrame(txframe);
                  rxframe = rxStation.nextTransmitFrame();
             System.out.printf("rxstation.nextTransmitFrame = %x %x %x %x %x\n",rxframe[0],rxframe[1],rxframe[2],rxframe[3],rxframe[4]);
             txStation.receiveFrame(rxframe);
    }
}
