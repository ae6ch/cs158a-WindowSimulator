import java.io.*;
import java.io.*;

import java.io.*;

class Window {

// Usage: WindowSimulator sws rws channel_length prob_not_recv prob_not_ackd num_frames

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: WindowSimulator sws rws channel_length prob_not_recv prob_not_ackd num_frames");
            System.exit(1);
        }       
        byte sws=Byte.parseByte(args[0]), rws=Byte.parseByte(args[1]);
        int channel_length=Integer.parseInt(args[2]);
        float prob_not_recv=Float.parseFloat(args[3]);
        float prob_not_ackd=Float.parseFloat(args[4]);
        int num_frames=Integer.parseInt(args[5]);
		
		if ((channel_length < sws) || (sws<1 || sws>127) || (rws<1 || rws>127) || (prob_not_recv < 0 || prob_not_recv > 1) || (prob_not_ackd < 0 || prob_not_ackd > 1)) {
            System.out.println("Invalid Inputs");
            System.exit(1);
        }
        
        Station sender = new Station(sws, rws, prob_not_ackd);
        Station receiver = new Station(sws, rws, prob_not_recv);
        Pipe senderPipe = new Pipe(channel_length);
        Pipe receiverPipe = new Pipe(channel_length);
        int steps=0;
        int counter=0;
        boolean notDone=true;
        float sumUtilizations=(float)0.0;
        
        while(notDone) {
            // Prints the word "Step" followed by the current value of step followed by a newline.
            System.out.printf("Step %d\n",steps);    
            
            // Prints "senderPipe" followed by a newline, followed by the result of calling printContents() on this Pipe, followed by a newline.
            System.out.println("senderPipe");
            senderPipe.printContents();
            System.out.println("");
            
           //  Prints "receiverPipe" followed by a newline, followed by the result of calling printContents() on this Pipe, followed by a newline.
            System.out.println("receiverPipe");
           receiverPipe.printContents();
            System.out.println("");

            // Adds the average of the senderPipe and receiverPipe utilization to sumUtilizations.
            sumUtilizations += (senderPipe.utilization()+receiverPipe.utilization())/2;
            //Checks if there is still data to send ( counter < num_frames) and that isReady() is true. If so, it calls send(counter) and increments counter.
            if ( (counter < num_frames) && sender.isReady()) {
                   sender.send(counter);
                   counter++;
            }
            
            // It calls the sender's nextTransmitFrame(), 
            // takes the byte array returned and calls senderPipe's addFrame method with it.
            // It takes the byte array returned from addFrame, and calls receiver's receiveFrame method using it.
            byte [] fromt=sender.nextTransmitFrame();
            receiver.receiveFrame(senderPipe.addFrame(fromt));
           
 
            // The receiver's nextTransmitFrame() method is then called, 
            // and the byte array from this is used to call the receiverPipe's addFrame method.
            // The returned frame from this addFrame call is then sent to the sender's receiveFrame method. 
            // If this frame was an acknowledgement for the num_frames - 1th sent frame, then notDone is set to false to terminate the while loop.
            // For our simulations we will use 5 byte frames: 1 byte for a sequence number, followed by 4 bytes of data. 
            // We will treat the frame seq_num 255 255 255 254 as an acknowledgement for the last frame to have sequence number seq_num. 
            // We will treat 255 255 255 255 255 as a non-frame frame (nothing being sent for one frame). The value MaxSeqNum used by your simulation should be set to 2*sws. 
            // A simulation involves the sender sending over the channels frames with data: 0, 1, 2, ..., num_frames - 1, and runs until all of the frames are acknowledged.
            byte[] fromRXtf=receiver.nextTransmitFrame();
            byte[] frame=receiverPipe.addFrame(fromRXtf);

            sender.receiveFrame(frame);

          
            //System.out.printf("Number of Acks Received so far: %d\n",sender.numSuccessfulAcks);
            if (sender.numSuccessfulAcks > num_frames-1) notDone=false;
        
            //If notDone is still false, steps should be incremented.
            if (notDone) {
                steps++;
            }
        } 
        // Once this loop completes, WindowSimulator should output the final value of steps 
        // and it should compute sumUtilizations/steps and output this as the average pipe utilization.
        System.out.printf("Steps %d\nAverage Pipe Utilization: %f%%\n",steps, (float)(sumUtilizations/steps)*100);
       
    }  
}
