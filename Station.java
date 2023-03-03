/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package window;

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
    
    public boolean isReady() {
        return (laf - lfr) <= rws;
    }
    public boolean send(int data) {
        for(int i = 0; i < timers.length; i++){
            timers[i] -= 1;
        }
        
        //cite this
        byte[] temp = ByteBuffer.allocateDirect(4).putInt(data).array();
        
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
        for(int i = 0; i < timers.length; i++){
            timers[i] -= 1;
        }
       
       int i = ((lfa + 1) % maxSeq) % rws;
       byte temp = (byte) 255;
       //255 might be a seq num for a real frame, check underneath commented lines
       while(rbuf[i] != (byte) 255 && i < rbuf.length){
            temp = rbuf[i];
            rbuf[i] = (byte) 255;
            rbuf[i+1] = (byte) 255;
            rbuf[i+2] = (byte) 255;
            rbuf[i+3] = (byte) 255;
            rbuf[i+4] = (byte) 255;
            i+=5;
       }
       if(temp != 255){
           byte[] ack = {temp, (byte) 255, (byte) 255, (byte) 255, (byte) 254};
           return ack;
       }
       
       temp = (byte) 255;
       i=0;
       for(int j = 0; j < timers.length; j++){
            if(timers[j] <= 0 && sbuf[j*5] < temp){
                temp = sbuf[j*5];
                i = j*5;
            }
        }
       
       if(temp != 255){
           byte[] resend = {sbuf[i], sbuf[i+1], sbuf[i+2], sbuf[i+3], sbuf[i+4]};
           timers[i/5] = TIMER;
           return resend;
       }
       
       i = 0;
       //
       while(rbuf[i] == (byte) 255  && i < rbuf.length){
           i+=5;
       }
       
       if(i < rbuf.length){
           byte[] send = {rbuf[i], rbuf[i+1], rbuf[i+2], rbuf[i+3], rbuf[i+4]};
           timers[i/5] = TIMER;
           return send;
       }
       
       byte[] non_frame = {(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
       return non_frame;
    }

    public void receiveFrame(byte[] frame){
        for(int i = 0; i < timers.length; i++){
            timers[i] -= 1;
        }
        if(frame[1] == 255 && frame[2] == 255 && frame[3] == 255 && frame[4] == 254){
            
            if((frame[0] > lar && frame[0] <= lfs && (lar <= lfs)) || (frame[0] > lar || frame[0] <= lfs && (lar > lfs))){
                
                for(int i = 0; i < sbuf.length; i+=5){
                    
                    if(sbuf[i] != (byte) 255)
                    if((lar > frame[0] && (sbuf[i] <= frame[0] || sbuf[i] > lar)) || (lar < frame[0] && (sbuf[i] <= frame[0] && sbuf[i] > lar))) {
                        
                        sbuf[i] = (byte) 255;
                        sbuf[i+1] = (byte) 255;
                        sbuf[i+2] = (byte) 255;
                        sbuf[i+3] = (byte) 255;
                        sbuf[i+4] = (byte) 255;
                    }
                }
                lar = frame[0];
            }
        }
        else {
            if((frame[0] <= lfr || frame[0] >= laf)){
                int index = frame[0] % rws;
                //
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
