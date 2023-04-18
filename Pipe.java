import java.util.*;

//Zayd Kudaimi  Shinhyung Lee  Steve Rubin 
public class Pipe {

    byte[] channel;
    public Pipe(int channelLength) {
        channel = new byte[5*channelLength];
        Arrays.fill(channel, (byte) 255);
    }
    
    public byte[] addFrame(byte[] frame) {
        byte[] retVal = new byte[5];
        System.arraycopy(channel,channel.length-5,retVal,0,5);  // stored the return value
        System.arraycopy(channel,0,channel,5,channel.length-5); // moved it over
        System.arraycopy(frame,0,channel,0,5); // add to frame begining
        return retVal;
    }
    public float utilization() { //returns the number of frames currently in the pipe that are not non-frame frames divided by the channelLength.
        float notFrame=0;
        for (int n=0; n < channel.length; n +=5) {
                //System.out.printf("%d=%02x %02x %02x %02x %02x\n",n,channel[n],channel[n+1],channel[n+2],channel[n+3],channel[n+4]);
            if (!((channel[n] == (byte)255) && (channel[n+1] == (byte)255) && (channel[n+2] == (byte)255) && (channel[n+3] == (byte)255) && (channel[n+4] == (byte)255)))  {
                notFrame++;
                //System.out.printf("%d=%02x %02x %02x %02x %02x is not a frame\n",n,channel[n],channel[n+1],channel[n+2],channel[n+3],channel[n+4]);
            }
        }
        //System.out.printf("\nnotframe=%f\n",notFrame);
        return notFrame/((float)channel.length/5.0f);
    }

    public void printContents() {
        for (int n=0; n < channel.length; n++)
            //System.out.printf("(n=%d)%02x\n",n,channel[n]);
            System.out.printf("%02x ",channel[n]);
    }
}
