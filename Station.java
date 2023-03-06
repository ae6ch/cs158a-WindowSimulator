/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.nio.ByteBuffer;
import java.util.Arrays;
/**
 *
 * @author zayd
 */
public class Station {
    final static int TIMER = 5;
    int maxSeq;
    //sender
    int sws; 
    int lar;
    int lfs;
    byte[] sbuf;
    int[] timers;
    
    //reciever
    int rws;
    int laf;
    int lfr;
    int lfa;
    byte[] rbuf;

    float propDrop;
        public Station(int sws, int rws, float propDrop) {
            this.maxSeq = 2*sws;
            this.sws = sws;
            this.rws = rws;
            this.propDrop = propDrop;
            sbuf = new byte[5*sws];
            timers = new int[sws];
            rbuf = new byte[5*rws];
            Arrays.fill(sbuf, (byte)255);
            Arrays.fill(rbuf, (byte)255);
            
            laf = rws;
            lfr = maxSeq-1;
            lfs = lfr;
            lar = lfs;
            
    }
    public void printbuf(byte [] buffer) {
                System.out.println("[sq] Payload");
        for (int idx=0; idx < sws; idx++) {
                int packet=idx*5;
                System.out.printf("[%d] %x %x %x %x\n",buffer[packet],buffer[packet+1],buffer[packet+2],buffer[packet+3],buffer[packet+4]);

        }
    }
    public boolean isReady() {
        return (laf - lfr) <= rws;
    }
    public boolean send(int data) {
        for(int i = 0; i < timers.length; i++){
             timers[i] -= 1;
            if (timers[i] >= 0)   timers[i] -= 1;

        }
        
        //cite this - why, we're not copying code just using a function the waymit was intended?
        byte[] temp = ByteBuffer.allocate(4).putInt(data).array();
        
        lfs = (lfs + 1) % maxSeq;
        int index = (lfs % sws)*5;
        if(sbuf[index] == (byte) 255){
            sbuf[index] = (byte) lfs;
            sbuf[index+1] = temp[0];
            sbuf[index+2] = temp[1];
            sbuf[index+3] = temp[2];
            sbuf[index+4] = temp[3];
          
            return true;
        } 
       
        return false;
    }

    public byte[] nextTransmitFrame() {
        byte [] sendCandidate;
        sendCandidate = new byte[5];  // The outgoing frame is stored here as we go
        boolean isAck = false;
        boolean isCandidate=false;    // set to true when we have a candidate in the buffer
                                    

        for(int i = 0; i < timers.length; i++){
            timers[i] -= 1; 
            if (timers[i] >= 0) timers[i] -= 1; 
            System.out.printf("timer%d = %d\n",i,timers[i]);
        }
       
      // int i = ((lfa + 1) % maxSeq) % rws;
      // byte temp = (byte) 255;
       //255 might be a seq num for a real frame, check underneath commented lines
      

      /* why is nextTransmitFrame writing into rbuf?
      while(rbuf[i] != (byte) 255 && i < rbuf.length){
            temp = rbuf[i];
            System.out.printf("temp is %x",temp);
            rbuf[i] = (byte) 255;
            rbuf[i+1] = (byte) 255;
            rbuf[i+2] = (byte) 255;
            rbuf[i+3] = (byte) 255;
            rbuf[i+4] = (byte) 255;
            i+=5;
       }
       */
    
       /* I don't think this works, it needs to look in rbuf to figure this out
       if(temp != (byte)255){
            System.out.printf("temp is %x, sending a ack",temp);
           byte[] ack = {temp, (byte) 255, (byte) 255, (byte) 255, (byte) 254};
           System.out.println("sending ack");
           return ack;
       }
       */
       
       //temp = (byte) 255;
       //i=0;
        /*
         1. There is an acknowledgment frame that could be sent.
            ***TODO****
        */
        int i = ((lfa + 1) % maxSeq) % rws;
        byte temp = (byte) -1;
       while(rbuf[i] != (byte) 255 && rbuf[i+1] == (byte) 255 && rbuf[i+2] == (byte) 255 && rbuf[i+3] == (byte) 255 && rbuf[i+4] == (byte) 255 && i < rbuf.length){
            temp = rbuf[i];
            System.out.printf("temp is %x",temp);
            rbuf[i] = (byte) 255;
            rbuf[i+1] = (byte) 255;
            rbuf[i+2] = (byte) 255;
            rbuf[i+3] = (byte) 255;
            rbuf[i+4] = (byte) 255;
            i+=5;
       }
       
    
       // I don't think this works, it needs to look in rbuf to figure this out
       if(temp != (byte)-1){
            System.out.printf("temp is %x, sending a ack",temp);
           byte[] ack = {temp, (byte) 255, (byte) 255, (byte) 255, (byte) 254};
           System.out.println("sending ack");
           return ack;
       }

       /*
        2. There is a frame in the sender window whose timer went off that has not yet been 
        resent. 
        3. Choose the oldest such frame that has not been resent. After choosing this 
        frame reset its timer.
        4. There is a frame in the sender window that has not yet been sent. 
        Choose the oldest such frame and start a timer for it.
        */

     
      
      
        for(int j = 0; j < timers.length; j++){
        System.out.printf("walking timer %d\n",j);
        //            if((timers[j] <= 0) && (sbuf[j*5] < temp)){
            //if((timers[j] <= 0) && (sbuf[j*5] < temp)){
            if(timers[j] <= 0)  {
                System.out.printf(" - expired");
                // Expired Timer, but non-frame  
                if ((sbuf[j*5]==(byte)  255) && (sbuf[j*5+1]==(byte)  255) && (sbuf[j*5+2]==(byte)  255) && (sbuf[j*5+3]==(byte)  255) &&(sbuf[j*5+4]==(byte) 255)) {
                  System.out.printf(" - non frame"); 
                }
                // Expired Timer, but is a sendable frame
                else if (sbuf[j*5] <= sendCandidate[0]) { // Pick the sendable frame with lowest seq
                    System.out.printf(" - frame w/seq %d",sbuf[j*5]); 
                    isCandidate=true;
                    System.arraycopy(sbuf,j*5,sendCandidate,0,5); // make the expired timer frame our candidate
                    System.out.printf(" - sendCandiate = %x %x %x %x %x",sendCandidate[0],sendCandidate[1],sendCandidate[2],sendCandidate[3],sendCandidate[4]);

                }
                else System.out.printf(" - frame w/seq %d is expired but older", sbuf[j*5]); 
            }
            System.out.printf("\n");
        }
    /* 
       if(temp != (byte)255){
           byte[] resend = {sbuf[i], sbuf[i+1], sbuf[i+2], sbuf[i+3], sbuf[i+4]};
           timers[i/5] = TIMER;
           System.out.println("resending something");
           return resend;
       }
    */
       
       /* 
              System.out.printf("sbuf.length=%d\n",sbuf.length);
       //while(rbuf[i] == (byte) 255  && i < rbuf.length){
        i = 0;
        while(i < sbuf.length){
            System.out.printf("i=%d\n",i);
           if (sbuf[i] == (byte) 255) i+=5;
       }
    
        System.out.printf("before send i=%d\n",i);
       if(i < sbuf.length){
           byte[] send = {sbuf[i], sbuf[i+1], sbuf[i+2], sbuf[i+3], sbuf[i+4]};
           timers[i/5] = TIMER;
           System.out.println("sending a packet");
           return send;
       }
       */
       
        /*
        After choosing the frame according to the above, nextTransmitFrame() generates a 
        random number between 0 and 1. If it is less than propDrop, nextTransmitFrame() 
        returns the non-frame frame; otherwise, it returns the frame chosen as above.

        Just set isCandidate back to false 
        */
       
       if (Math.random() < propDrop) isCandidate = false;

       // did we ever put a frame into sendCandidate?
       // can't just see if its 0,0,0,0,0 because thats a valid sendable frame
       if (isCandidate) { 
            // (re)set the timer to whatever we are sending to TIMER
            timers[sendCandidate[0]] = TIMER;
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
        for(int i = 0; i < timers.length; i++) {
            if (timers[i] >= 0) timers[i] -= 1;
        }
        //lar - Last ACK RX
        //lfs - Last Frame Sent
        if(frame[1] == 255 && frame[2] == 255 && frame[3] == 255 && frame[4] == 254) {           //RX Frame is ACK
            System.out.println("got a ack");
            /* 
             if((frame[0] > lar && frame[0] <= lfs && (lar <= lfs)) || (frame[0] > lar || frame[0] <= lfs && (lar > lfs))){                
                
                for(int i = 0; i < sbuf.length; i+=5){
                    
                if(sbuf[i] != (byte) 255)
                    if((lar > frame[0] && (sbuf[i] <= frame[0] || sbuf[i] > lar)) || (lar < frame[0] && (sbuf[i] <= frame[0] && sbuf[i] > lar))) {
                    
                        sbuf[i] = (byte) 255;          
                        sbuf[i+1] = (byte) 255;
                        sbuf[i+2] = (byte) 255;
                        sbuf[i+3] = (byte) 255;
                        sbuf[i+4] = (byte) 254;
                    }
                }
                */

            lar = frame[0];        // sent lar to the ack seq just received
            
        }
        else {
            if(frame[0] == (byte)255 && frame[1] == (byte)255 && frame[2] == (byte)255 && frame[3] == (byte)255 && frame[4] == (byte)255) {
                // Not a ACK, Not a Non-Frame
                // placeholder incase we want to do anything here
                System.out.println("received non-frame");
            }
            else if((frame[0] <= lfr || frame[0] >= laf)) {
                int index = frame[0] % rws;
                System.out.printf("Receiving data frame f0=%x lfr=%x laf=%x index=%d\n",frame[0],lfr,laf,index);

                if(rbuf[index] == (byte) 255){
                    rbuf[index] = frame[0];
                    rbuf[index+1] = frame[1];
                    rbuf[index+2] = frame[2];
                    rbuf[index+3] = frame[3];
                    rbuf[index+4] = frame[4];
                }
            }
        }
    }
}
