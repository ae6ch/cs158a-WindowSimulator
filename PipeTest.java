//import Pipe.*;
import java.util.*;

public class PipeTest {
        public static void main(String[] args) {
        int channel_length=3;
        Pipe testPipe = new Pipe(channel_length);
        
        System.out.println("printContents() should be all f's -------------------------\n");
        testPipe.printContents();
        System.out.println("");
        System.out.println("utilization() should output 0 -------------------------\n");
        System.out.println(testPipe.utilization()); 
        System.out.println("");
    
        byte[] tmpframe={1,2,3,4,5};
        byte[] tmpframe2={0xa,0xa,0xa,0xa,0xa};
        byte[] tmpframe3={0xb,0xb,0xb,0xb,0xb};
        byte[] tmpframe4={0xc,0xc,0xc,0xc,0xc};

        System.out.printf("",testPipe.addFrame(tmpframe));
        testPipe.addFrame(tmpframe);
        testPipe.addFrame(tmpframe2);
        testPipe.addFrame(tmpframe3);
        byte[] pushedoffFrame = testPipe.addFrame(tmpframe4);
        System.out.println("utilization() should output 1 after 4 addframes -------------------------\n");
        System.out.println(testPipe.utilization()); 

        System.out.println("printContents() should be 5x each of 0xc 0xb 0xa ------------------------\n");
        testPipe.printContents();
        System.out.printf("\npushed off frame should be 12345\n");
        System.out.printf("%02x %02x %02x %02x %02x\n",pushedoffFrame[0],pushedoffFrame[1],pushedoffFrame[2],pushedoffFrame[3],pushedoffFrame[4]);

        }
}
