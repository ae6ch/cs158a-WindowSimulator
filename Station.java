
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Station {

    static int TIMER = 10;
    int numSuccessfulAcks=0;
    byte maxSeq;
    //sender
    byte sws;        // Sender Window Size - the maximum number of unacknowledged frames that the sender can transmit. We will choose the SWS to try to keep the pipe as full as possible
    byte lar;        // Last Ack Received - the sequence number of the last frame acknowledged. 
    byte lfs;        // last frame sent (LFS) -- the sequence number of the last frame sent
    byte[] senderBuffer;
    int[] timers;   
    int[] age;      // Holds the age of a frame in the buffer location
    int[] resends;  // Number of times the frame stored have been resent 
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
        resends = new int[sws]; 
        receiverBuffer = new byte[5*rws];
        Arrays.fill(senderBuffer, (byte)255);
        Arrays.fill(receiverBuffer, (byte)255);
        Arrays.fill(timers, (byte) -1);
        Arrays.fill(age, (byte) -1);
        Arrays.fill(resends, (byte) 0);


        laf = rws;          // The receiver maintains that LAF - LFR ≤ RWS  
        lfr = -1;
        lfs = -1;
        lar = -1;
        SeqNumToAck = -1;
            
    }

    public boolean isReady() { 
        // returns whether the Station can receive a new frame to queue - free buffer space, and lfs - lar <= sws
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

                    return send && recieve; // in window and buffer space
   
    }

    public boolean send(int data) {
        byte[] sendTemp = ByteBuffer.allocate(4).putInt(data).array();
       

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
                //timers[i/5 ] = TIMER;         // Queue Timer
                age[i/5] = 0;                // Set Age to 0
                resends[i/5] = -1;            // We've never resent this frame
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

     //   System.out.printf("Timers ");
        for(int i = 0; i < timers.length; i++){ // Decrease timers that are active
                timers[i]--;
                 //System.out.printf(" %d",timers[i]);
                if (timers[i] < -1) timers[i] = TIMER;
                
                
        }
       // System.out.printf("\nAge ");
        for(int i = 0; i < age.length; i++){    // Increase age of all frames that are active
                age[i]++;
                //System.out.printf(" %d",age[i]);

        }
      //  System.out.printf("\n");
     //   System.out.printf("Senderbuffer: ");
        //for (int ic = 0; ic<senderBuffer.length; ic++) {
          //  System.out.printf("%x ",senderBuffer[ic]);
        //}
       // System.out.printf("\n");

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
          //  System.out.printf("Send ACK %d\n",SeqNumToAck);
            sendCandidate[0] = SeqNumToAck;
            sendCandidate[1] = (byte) 255;
            sendCandidate[2] = (byte) 255;
            sendCandidate[3] = (byte) 255;
            sendCandidate[4] = (byte) 254;
            isCandidate=true;

            SeqNumToAck = -1;

        }  
    
        if (!(isCandidate)) {           // Don't do this if we have a ACK pending transmision
           // System.out.printf("No Pending ack\n");
            int oldestAge=0;
            int oldestResendAge=0;
            int oldestResendAgePos=-1;
            int oldestAgePos=-1;

            for(int i = 0; i < timers.length; i++){ 
                int ibuf=i*5; 
                if (timers[i] <= 0) {       // Frame stored in i has expired timer
                    //System.out.printf("Timer %d expired\n",i);
                    if ((senderBuffer[ibuf] == (byte)255) && (senderBuffer[ibuf+1] == (byte)255) && (senderBuffer[ibuf+2] == (byte)255) && (senderBuffer[ibuf+3] == (byte)255) && (senderBuffer[ibuf+4] == (byte)255)) {
                        // Frame is a non-frame, ignore
                        timers[i] = -1;
                    } 
                    if ((senderBuffer[ibuf+1] == (byte)255) && (senderBuffer[ibuf+2] == (byte)255) && (senderBuffer[ibuf+3] == (byte)255) && (senderBuffer[ibuf+4] == (byte)254)) {
                        // Frame is a ack, set to non-frame and ignore
                        // 
                        senderBuffer[ibuf]=(byte) 255;
                        senderBuffer[ibuf+4]=(byte) 255;
                        timers[i] = -1;
                     }

                  //  if ((resends[i] >= 1) && (isCandidate==false)) {    // #2-3 Find oldest frame that has been resent, and then resend it again  
                    if(isCandidate==false) {           
                        //System.out.printf("Looking at %d for age=%d and oldestresendage=%d\n",i,age[i],oldestResendAge);
                        //System.out.printf("is %d greater then %d?\n",i,age[i],oldestResendAge);
                        if (age[i] > oldestResendAge) {
                            //System.out.println("resend");
                            oldestResendAge=age[i];
                            oldestResendAgePos=i;
                            sendCandidate[0] = senderBuffer[ibuf];
                            sendCandidate[1] = senderBuffer[ibuf+1];
                            sendCandidate[2] = senderBuffer[ibuf+2];
                            sendCandidate[3] = senderBuffer[ibuf+3];
                            sendCandidate[4] = senderBuffer[ibuf+4];
                            isCandidate=true;                   
                        }
                    } 
                   if ((resends[i] <= 0) && (isCandidate==false)) {     // #4 ind oldest frame that has never been sent, and then send it
                         //System.out.printf("Looking at %d for non resends age=%d and oldestAge=%d\n",i,age[i],oldestAge);

                         //System.out.printf("is %d greater then %d?\n",i,age[i],oldestAge);
                        if (age[i] > oldestAge) {
                            System.out.println("not resend");
                            oldestAge=age[i];
                            oldestAgePos=i;
                            sendCandidate[0] = senderBuffer[ibuf];
                            sendCandidate[1] = senderBuffer[ibuf+1];
                            sendCandidate[2] = senderBuffer[ibuf+2];
                            sendCandidate[3] = senderBuffer[ibuf+3];
                            sendCandidate[4] = senderBuffer[ibuf+4];
                            isCandidate=true;
                            //System.out.printf("A 1 sendCandidate = %d %d %d %d %d\n",sendCandidate[0],sendCandidate[1],sendCandidate[2],sendCandidate[3],sendCandidate[4]);

                        }
                    }
                }
            }
            if (isCandidate) {  // If we now have a frame to send and its a resend, need to increment resend for the frame
                if (oldestResendAge > 0) {  // This is a resend
                //System.out.printf("DEBUG Q FOR RESEND\n");
                resends[oldestResendAgePos]++;
                timers[oldestResendAgePos] = TIMER;
                }
                if (oldestAge > 0) {  // This isthe initial send
                    //System.out.printf("DEBUG Q FOR SEND\n");

                    timers[oldestAgePos] = TIMER;
                    resends[oldestAgePos]=0;
                }
                
            }

        }
       
        /*
        After choosing the frame according to the above, nextTransmitFrame() generates a 
        random number between 0 and 1. If it is less than propDrop, nextTransmitFrame() 
        returns the non-frame frame; otherwise, it returns the frame chosen as above.

        */
       
       if ((float) Math.random() < propDrop) { 
         //System.out.printf("DEBUG*** DROPPING FRAME %x %x %x %x %x ***\n", sendCandidate[0],sendCandidate[1],sendCandidate[2],sendCandidate[3],sendCandidate[4]);
        byte[] non_frame = {(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
        return non_frame; 
       } //else System.out.printf("DEBUG*** NOT DROPPING FRAME %x %x %x %x %x ***\n", sendCandidate[0],sendCandidate[1],sendCandidate[2],sendCandidate[3],sendCandidate[4]);
       

       if (isCandidate) { 
       // System.out.printf("A 2 sendCandidate = %d %d %d %d %d\n",sendCandidate[0],sendCandidate[1],sendCandidate[2],sendCandidate[3],sendCandidate[4]);

            return sendCandidate;
       }

       // we never picked a candidate to send, OR because propdrop math send non_frame 
        byte[] non_frame = {(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
        return non_frame; 
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
               // System.out.println("got a in window ack-frame");
                numSuccessfulAcks++;
                //System.out.printf("DEBUG Number of Acks Received so far: %d\n",numSuccessfulAcks);

                lar=SeqNum;

                //System.out.printf("Sendbuffer BEFORE:\n");
                //for (int ic = 0; ic<senderBuffer.length; ic++) {
                //    System.out.printf("%x ",senderBuffer[ic]);
                //}
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
                //for (int ib = 0; ib<senderBuffer.length; ib++) {
                //     System.out.printf("%x ",senderBuffer[ib]);
                //}
                //System.out.printf("\n");
                    
            }
            return;
        }
        
    
    
        // The Frame is in the window
        //System.out.printf("got a in window frame %d for %d and %d",SeqNum,lfr,laf);

        // Receive frame and put it into buffer
        for (int i=0; i<receiverBuffer.length; i+=5)  { // find the first free receiverBuffer entry  
            if ((receiverBuffer[i] != (byte)255) && (receiverBuffer[i+1] != (byte)255) && (receiverBuffer[i+2] != (byte)255) && (receiverBuffer[i+3] != (byte)255) && (receiverBuffer[i+4] != (byte)255)) {
                //receiverBuffer[i] = frame[0];
                //receiverBuffer[i+1] = frame[1];
                //receiverBuffer[i+2] = frame[2];
                //receiverBuffer[i+3] = frame[3];
                // receiverBuffer[i+4] = frame[4];
            }
            SeqNumToAck=SeqNum;
            //System.out.printf("ZZZ BEFORE LFR lfr=%d laf=%d receive frame %d %d %d %d %d\n",lfr,laf,frame[0],frame[1],frame[2],frame[3],frame[4]);

            lfr=SeqNumToAck;

            laf=(byte)(lfr+1+rws);
            //System.out.printf("ZZZ AFTER LAF lfr=%d laf=%d receive frame %d %d %d %d %d\n",lfr,laf,frame[0],frame[1],frame[2],frame[3],frame[4]);


        }    
    
    }
}
