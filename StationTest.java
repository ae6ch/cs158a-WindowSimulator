import java.io.*;
import java.util.*;

public class StationTest {
    public static void main(String[] args) {
        Station txStation = new Station((byte)5,(byte)3,(float)0);
        Station rxStation = new Station((byte)5,(byte)3,(float)0);


        System.out.printf("isReady()=%b\n",txStation.isReady());
     
       txStation.send(0xa);
 


       
//       txStation.send(0xf);

       for (int i=0; i < txStation.senderBuffer.length; i++) {
          System.out.printf("%d ",txStation.senderBuffer[i]);
       }
       System.out.printf("\n");
;
  
    byte[] txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
     rxStation.receiveFrame(txframe);
     txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
     rxStation.receiveFrame(txframe);
     txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
     rxStation.receiveFrame(txframe);
     txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
     rxStation.receiveFrame(txframe);
     txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
     rxStation.receiveFrame(txframe);
     txframe = txStation.nextTransmitFrame();
     System.out.printf("txstation.nextTransmitFrame = %x %x %x %x %x\n",txframe[0],txframe[1],txframe[2],txframe[3],txframe[4]);
     rxStation.receiveFrame(txframe);




     }
}
   