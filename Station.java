import java.nio.ByteBuffer;
import java.util.Arrays;
/**
 *
 * @author zayd
 */
public class Station {

    static int TIMER = 5;  
    int sentFramesTotal=0;
    int rxFramesTotal=0;
    int sentAcksTotal=0;
    int numberOfFramesAck=0;
    int numSuccessfulAcks=0;
    byte maxSeq;
    //sender
    byte sws;        // Sender Window Size - the maximum number of unacknowledged frames that the sender can transmit. We will choose the SWS to try to keep the pipe as full as possible
    byte lar;        // Last Ack Received - the sequence number of the last frame acknowledged. 
    byte lfs;        // last frame sent (LFS) -- the sequence number of the last frame sent
    byte[] senderBuffer;
    int[] timers;   
    int[] age;      // Holds the age of a frame in the buffer location
    byte SeqNumToAck;
    
    //reciever
    byte rws;        // receive window size (RWS) -- maximum number of out of order frame the receiver is willing to receive
    byte laf;        // largest acceptable frame (LAF) -- the highest sequence number that the receiver is currently willing to accept.
    byte lfr;        // last frame received (LFR) -- the sequence number of last frame received.
    byte[] receiverBuffer;

    float propDrop;
    public Station(byte sws, byte rws, float propDrop) {
        this.maxSeq = (byte)(2*sws);
        this.sws = sws;
        this.rws = rws;
        this.propDrop = propDrop;
        senderBuffer = new byte[5*sws];
        timers = new int[sws];
        age = new int[sws];     
        receiverBuffer = new byte[5*rws];
        Arrays.fill(senderBuffer, (byte)255);
        Arrays.fill(receiverBuffer, (byte)255);
        Arrays.fill(timers, (byte) -1);
        Arrays.fill(age, (byte) -1);


        laf = rws;          // The receiver maintains that LAF - LFR ≤ RWS  
        lfr = -1;
        lfs = -1;
        lar = -1;
        SeqNumToAck = -1;
            
    }

    public boolean isReady() { 
        // returns whether the Station can receive a new frame to queue, since we only use station as a sender or reciever, either send or receive will always be true, allowing the function to return the status of the correct buffer if called on the sender or receiver
      boolean send = false;
      boolean recieve = false;
      
      for(int i = 0; i < senderBuffer.length; i+=5){
          if(senderBuffer[i] == (byte)255 && senderBuffer[i+1] == (byte)255 && senderBuffer[i+2] == (byte)255 && senderBuffer[i+3] == (byte)255 && senderBuffer[i+4] == (byte)255){
              send = true;
              break;
          }
      }
      for(int i = 0; i < receiverBuffer.length; i+=5){
          if(receiverBuffer[i] == (byte)255 && receiverBuffer[i+1] == (byte)255 && receiverBuffer[i+2] == (byte)255 && receiverBuffer[i+3] == (byte)255 && receiverBuffer[i+4] == (byte)255){
              recieve = true;
              break;
          }
      }
//  if (lfs - lar <= sws) {  
           // for (int i=0; i<receiverBuffer.length; i+=5) { // returns true once we find one free entry in the receiverBuffer 
           //     if ((receiverBuffer[i] == (byte)255) && (receiverBuffer[i+1] == (byte)255) && (receiverBuffer[i+2] == (byte)255) && (receiverBuffer[i+3] == (byte)255) && (receiverBuffer[i+4] == (byte)255))
                    return send && recieve; // in window and buffer space
           //     }
       // }
     //   return false;  // not in window, and/or no buffer space
    }

    public boolean send(int data) {
        byte[] sendTemp = ByteBuffer.allocate(4).putInt(data).array();
        for(int ia = 0; ia < age.length; ia++){    // Increase age of all frames that are active, decrease all timers
            age[ia]++;

         // if (timers[ia]>=0) timers[ia]--;
        }

        int i;
        
        for (i=0; i<senderBuffer.length; i+=5) { // find the first free sendbuffer entry
            if ((senderBuffer[i] == (byte)255) && (senderBuffer[i+1] == (byte)255) && (senderBuffer[i+2] == (byte)255) && (senderBuffer[i+3] == (byte)255) && (senderBuffer[i+4] == (byte)255)) {
                lfs = (byte) ((lfs+1) % maxSeq);                    // Increase SeqNum, wrap to SWS
                //System.out.printf("new LFS is %x\n",lfs);
                senderBuffer[i] = lfs;
                senderBuffer[i+1] = sendTemp[0];
                senderBuffer[i+2] = sendTemp[1];
                senderBuffer[i+3] = sendTemp[2];
                senderBuffer[i+4] = sendTemp[3];
                //System.out.printf("A  Senderbuffer = %d %d %d %d %d\n",senderBuffer[i],senderBuffer[i+1],senderBuffer[i+2],senderBuffer[i+3],senderBuffer[i+4]);
                age[i/5] = 0;                // Set Age to 0
                timers[i/5]=-1;
            return true;
            }
        } 
       
        return false; 
    }

    public byte[] nextTransmitFrame() {
        byte [] sendCandidate;
        sendCandidate = new byte[5];  // The outgoing frame is stored here as we go
        Arrays.fill(sendCandidate, (byte)255);
        boolean isCandidate=false;    // set to true when we have a candidate in the buffer

     //   System.out.printf("Sendbuffer:\n");
     //   for (int i = 0; i<senderBuffer.length; i++) {
     //       System.out.printf("%d ",senderBuffer[i]);
     //   }
     //   System.out.printf("\n");

        //System.out.printf("Timers ");
        for(int i = 0; i < timers.length; i++){ // Decrease timers that are active
                
                if(timers[i] > 0) timers[i]--;
                // System.out.printf(" %d",timers[i]);    
        }
        //System.out.printf("\nAge ");
        for(int i = 0; i < age.length; i++){    // Increase age of all frames that are active
                age[i]++;
                //System.out.printf(" %d",age[i]);

        }
       // System.out.printf("\n");
        //System.out.printf("Senderbuffer: ");
        for (int ic = 0; ic<senderBuffer.length; ic++) {
            //System.out.printf("%x ",senderBuffer[ic]);
        }
      //  System.out.printf("\n");

        /*
         1. There is an acknowledgment frame that could be sent.
        2. There is a frame in the sender window whose timer went off that has not yet been 
        resent. 
        3. Choose the oldest such frame that has not been resent. After choosing this 
        frame reset its timer.
        
        4. There is a frame in the sender window that has not yet been sent. 
        Choose the oldest such frame and start a timer for it.
        */
        if (SeqNumToAck > -1) {             // We have a frame to ack
            //System.out.printf("Send ACK %d\n",SeqNumToAck);
            sendCandidate[0] = SeqNumToAck;
            sendCandidate[1] = (byte) 255;
            sendCandidate[2] = (byte) 255;
            sendCandidate[3] = (byte) 255;
            sendCandidate[4] = (byte) 254;
            isCandidate=true;
           //lfr=SeqNumToAck;
           // laf=(byte) ((lfr + rws) % rws); // Account for wrap
            SeqNumToAck = -1;

        }  
        else{
            int old = Integer.MIN_VALUE;
            boolean flag = false;
            int j = -1;
            for(int i = 0; i< timers.length; i++){
                if(timers[i] == 0){
                    int index = i*5;
                    if(!(senderBuffer[index] == (byte)255 && senderBuffer[index+1] == (byte)255 && senderBuffer[index+2] == (byte)255 && senderBuffer[index+3] == (byte)255 && senderBuffer[index+4] == (byte)255)){
                        if(age[i]> old){
                            old = age[i];
                            sendCandidate[0] = senderBuffer[index];
                            sendCandidate[1] = senderBuffer[index];
                            sendCandidate[2] = senderBuffer[index];
                            sendCandidate[3] = senderBuffer[index];
                            sendCandidate[4] = senderBuffer[index];
                            flag = true;
                            j=i;
                        }
                    }
                }
            }
            if( j != -1){
                timers[j]=TIMER;
            }
            if(!flag){
            old = Integer.MIN_VALUE;
            j = -1;
            for(int i = 0; i< timers.length; i++){
                if(timers[i] < 0){
                    int index = i*5;
                    if(!(senderBuffer[index] == (byte) 255 && senderBuffer[index+1] == (byte) 255 && senderBuffer[index+2] == (byte) 255 && senderBuffer[index+3] == (byte) 255 && senderBuffer[index+4] == (byte) 255)){
                        if(age[i]> old){
                            old = age[i];
                            sendCandidate[0] = senderBuffer[index];
                            sendCandidate[1] = senderBuffer[index];
                            sendCandidate[2] = senderBuffer[index];
                            sendCandidate[3] = senderBuffer[index];
                            sendCandidate[4] = senderBuffer[index];
                            flag = true;
                            j=i;
                        }
                    }
                }
            }
            if( j != -1){
                timers[j]=TIMER;
            }
            }
        }
        
       
        /*
        After choosing the frame according to the above, nextTransmitFrame() generates a 
        random number between 0 and 1. If it is less than propDrop, nextTransmitFrame() 
        returns the non-frame frame; otherwise, it returns the frame chosen as above.
        */
       
       if ((float) Math.random() < propDrop) { 
         //System.out.printf("*** DROP FRAME ***\n");
        byte[] non_frame = {(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
        return non_frame; 
       }

       // we never picked a candidate to send, OR because propdrop math send non_frame 
        byte[] non_frame = {(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
        return sendCandidate; 
    }


    /* 
    if the frame is an acknowledgement frame, then this methods updates the variables associated with the sender window accordingly.
    If the frame is a data frame and its sequence number in the frame is within the receiver window, adds the frame to the Station's 
    receiverBuffer and updates variables associated with the receiver window (including remembering enough, so that nextTransmitFrame() 
    can send any necessary acknowledgment frames).
   */
    public void receiveFrame(byte[] frame) {
        byte SeqNum=frame[0];


        //System.out.printf("lfr=%d laf=%d receive frame %d %d %d %d %d\n",lfr,laf,frame[0],frame[1],frame[2],frame[3],frame[4]);
        if ((frame[0] == (byte)255) && (frame[1] == (byte)255) && (frame[2] == (byte)255) && (frame[3] == (byte)255) && (frame[4] == (byte)255)) { //Frame is non-frame
                //System.out.println("got a non-frame");
                return;
        } 

        if (laf > maxSeq) 
        if (((SeqNum  <= lfr) || ((SeqNum) > laf)))
       if ((laf>maxSeq) && (SeqNum >= (laf-(lfr+1))))
        {
            //System.out.printf("got a out of window frame %d for %d and %d\n",SeqNum,lfr,laf);
           // this frame is out of window and is discarded
           return;
        }    

        if ((frame[1] == (byte)255) && (frame[2] == (byte)255) && (frame[3] == (byte)255) && (frame[4] == (byte)254)) { //Frame is ACK
            //System.out.printf("got a ack-frame %d %d %d\n",SeqNum,lfr,laf);
           // if ((SeqNum > lar) && (SeqNum < lfs)) 
            if(true)  { // ack is in window
                //System.out.println("got a in window ack-frame");
                numSuccessfulAcks++;
                lar=SeqNum;

                //System.out.printf("Sendbuffer BEFORE:\n");
                for (int ic = 0; ic<senderBuffer.length; ic++) {
                    //System.out.printf("%x ",senderBuffer[ic]);
                }
                //System.out.printf("\n");


                // find the frame and remove it from the sender buffer
                for (int io=0; io<senderBuffer.length; io+=5) {    
                    //System.out.printf("clearing sb, i=%d lar=%d\n",io,lar);
                    if ((senderBuffer[io] == lar)) {
                        senderBuffer[io] = (byte) 255;
                        senderBuffer[io+1] = (byte) 255;
                        senderBuffer[io+2] = (byte) 255;
                        senderBuffer[io+3] = (byte) 255;
                        senderBuffer[io+4] = (byte) 255;
                        age[io/5] = -1;
                    }
                
                }

                //System.out.printf("Sendbuffer AFTER:\n");
                for (int ib = 0; ib<senderBuffer.length; ib++) {
                     //System.out.printf("%x ",senderBuffer[ib]);
                }
                //System.out.printf("\n");
                    
            }
            return;
        }
        
       // if (laf > maxSeq) 
      //  if (((SeqNum  <= lfr) || ((SeqNum) > laf)))
       //     if ((laf>maxSeq) && (SeqNum >= (laf-(lfr+1))))
       //{
       //     System.out.printf("got a out of window frame %d for %d and %d\n",SeqNum,lfr,laf);
       //     // this frame is out of window and is discarded
        //    return;
      //  } 
    
        // The Frame is in the window
        //System.out.printf("got a in window frame %d for %d and %d",SeqNum,lfr,laf);

        // Receive frame and put it into buffer
        for (int i=0; i<receiverBuffer.length; i+=5)  { // find the first free receiverBuffer entry  
            if ((receiverBuffer[i] == (byte)255 && receiverBuffer[i+1] == (byte)255 && receiverBuffer[i+2] == (byte)255 && receiverBuffer[i+3] == (byte)255 && receiverBuffer[i+4] == (byte)255)) {
                receiverBuffer[i] = frame[0];
                receiverBuffer[i+1] = frame[1];
                receiverBuffer[i+2] = frame[2];
                receiverBuffer[i+3] = frame[3];
                 receiverBuffer[i+4] = frame[4];
                 break;
            }
        }
        for (int i=0; i<receiverBuffer.length-5; i+=5){
            for (int j=0; j<receiverBuffer.length-i-5; j+=5){
            if(receiverBuffer[j]> receiverBuffer[j+5]){
                byte[] temp = {receiverBuffer[j], receiverBuffer[j+1], receiverBuffer[j+2], receiverBuffer[j+3], receiverBuffer[j+4]};
                
                receiverBuffer[j]= receiverBuffer[i];
                receiverBuffer[j+1]= receiverBuffer[i+1];
                receiverBuffer[j+2]= receiverBuffer[i+2];
                receiverBuffer[j+3]= receiverBuffer[i+3];
                receiverBuffer[j+4]= receiverBuffer[i+4];
                
                receiverBuffer[i]= temp[0];
                receiverBuffer[i+1]= temp[0+1];
                receiverBuffer[i+2]= temp[0+2];
                receiverBuffer[i+3]= temp[0+3];
                receiverBuffer[i+4]= temp[0+4];
            }
        }
        }
        for (int i=0; i<receiverBuffer.length; i+=5)  { // find the first free receiverBuffer entry  

            if(SeqNumToAck == -1)
            SeqNumToAck= receiverBuffer[i];
            else if(SeqNumToAck+1 == receiverBuffer[i])
                SeqNumToAck++;
            //System.out.printf("ZZZ BEFORE LFR lfr=%d laf=%d receive frame %d %d %d %d %d\n",lfr,laf,frame[0],frame[1],frame[2],frame[3],frame[4]);

            lfr=SeqNumToAck;

            laf=(byte)(lfr+1+rws);
            //System.out.printf("ZZZ AFTER LAF lfr=%d laf=%d receive frame %d %d %d %d %d\n",lfr,laf,frame[0],frame[1],frame[2],frame[3],frame[4]);


        }    
    
    }
}
