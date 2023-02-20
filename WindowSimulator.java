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
		
		if ((channel_length < sws) || (sws<0 && sws>127) || (rws<0 && rws>127) || (prob_not_recv < 0 && prob_not_recv > 1) || (prob_not_ackd < 0 && prob_not_ackd > 1)) {
            System.out.println("Invald Inputs");
            System.exit(1);
        }

        int MaxSeqNum=sws*2;
    }
}
