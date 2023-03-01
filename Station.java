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
    int maxSeq;
    //sender
    int sws;
    int lar;
    int lfs;
    byte[] sbuf;
    
    //reciever
    int rws;
    int laf;
    int lfr;
    byte[] rbuf;

    float propDrop;
        public Station(int sws, int rws, float propDrop) {
            this.maxSeq = 2*sws;
            this.sws = sws;
            this.rws = rws;
            this.propDrop = propDrop;
            sbuf = new byte[5*sws];
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
        //cite this
        byte[] temp = ByteBuffer.allocateDirect(4).putInt(data).array();
        
        lfs += 1;
        if(lfs == maxSeq){
            lfs = 0;
        }
        int index = lfs % sws;
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
       
    }

    public void receiveFrame(byte[] frame){
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
