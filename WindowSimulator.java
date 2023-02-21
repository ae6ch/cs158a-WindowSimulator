import java.io.*;
import Pipe.*;
import Station.*

class WindowSimulator {

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
            System.out.println("Invald Inputs");
            System.exit(1);
        }
        
        Station sender = new Station(sws, rws, prob_not_ackd);
        Station receiver = new Station(sws, rws, prob_not_recv);
        Pipe senderPipe = new Pipe(channel_length);
        Pipe receiverPipe = new Pipe(channel_length);
        int MaxSeqNum=sws*2;
        int steps=0;
        int counter=0;
        boolean notDone=true;
        int sumUtilizations=0;
        
        while(notDone) {
            // Prints the word "Step" followed by the current value of step followed by a newline.
            System.out.printf("Step %d",steps);
            
            // Prints "senderPipe" followed by a newline, followed by the result of calling printContents() on this Pipe, followed by a newline.
            System.out.println("senderPipe");
            senderPipe.printContents();
            System.out.println("");
            
            // Prints "receiverPipe" followed by a newline, followed by the result of calling printContents() on this Pipe, followed by a newline.
            System.out.println("receiverPipe");
            receiverPipe.printContents();
            System.out.println("");

            // Adds the average of the senderPipe and receiverPipe utilization to sumUtilizations.
            sumUtilizations += (senderPipe.utilization+receiverPipe.utilization)/2;
            
            //Checks if there is still data to send ( counter < num_frames) and that isReady() is true. If so, it calls send(counter) and increments counter.
            if (counter < num_frames) && senderPipe.isReady() && receiverPipe.isReady() {
                    sender.send(counter++);
            }
            
            // It calls the sender's nextTransmitFrame(), 
            // takes the byte array returned and calls senderPipe's addFrame method with it.
            // It takes the byte array returned from addFrame, and calls receiver's receiveFrame method using it.
            receiver.receiveFrame(senderPipe.addFrame(sender.nextTransmitFrame()));



            // The receiver's nextTransmitFrame() method is then called, 
            // and the byte array from this is used to call the receiverPipe's addFrame method.
            // The returned frame from this addFrame call is then sent to the sender's receiveFrame method. 
            sender.receiveFrame(receiverPipe.addFrame(receiver.nextTrasmitFrame()));

            //If this frame was an acknowledgement for the num_frames - 1th sent frame, then notDone is set to false to terminate the while loop.
            // *TODO*
            if (something) {
                notDone=false;

            }

            //If notDone is still false, steps should be incremented.
            if (!notDone) {
                steps++;
            }
        } 
        // Once this loop completes, WindowSimulator should output the final value of steps 
        // and it should compute sumUtilizations/steps and output this as the average pipe utilization.
        system.out.printf("Steps %d\nAverage Pipe Utilization: %f",steps, sumUtilizations/steps);
        
    }
}