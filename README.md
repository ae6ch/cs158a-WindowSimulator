#
# SJSU Spring 2023 CS158A Section 2 Homework 2
# cs158a-WindowSimulator
For the coding portion of the homework I'd like you to write a Java program WindowSimulator.java that allows users to conduct experiments involving the sliding window protocol. Your program will be compiled from the command line with a syntax:

javac WindowSimulator.java
Once compiled, it will be run from the command line with a line like:
java WindowSimulator sws rws channel_length prob_not_recv prob_not_ackd num_frames
Here sws is the sender window size and rws is the receiver window size. We assume these are values between 1 and 127. channel_length is the number of frames that can be in-flight from the sender to receiver and vice versa. prob_not_recv is a float between 0 and 1 indicating the odds that a frame from the sender to the receiver disappears. prob_not_ackd is a float between 0 and 1 indicating the odds that a frame from the receiver to the sender disappears. num_frames is the number of frames that are to be sent from the sender to the receiver during the simulation, we will assume this number can be stored in a 4 byte int.

Below is an example line that might be used to do a simulation experiment:

java WindowSimulator 5 3 6 0.1 0.2 10 
If in the command line arguments have channel_length is less than sws, or if there are any other kind of inputs that don't match the value ranges mentioned above, your program should stop and say "Invalid Inputs".

For our simulations we will use 5 byte frames: 1 byte for a sequence number, followed by 4 bytes of data. We will treat the frame seq_num 255 255 255 254 as an acknowledgement for the last frame to have sequence number seq_num. We will treat 255 255 255 255 255 as a non-frame frame (nothing being sent for one frame). The value MaxSeqNum used by your simulation should be set to 2*sws. A simulation involves the sender sending over the channels frames with data: 0, 1, 2, ..., num_frames - 1, and runs until all of the frames are acknowledged.

To conduct a simulation, your code should define and make use of the two classes: Station and Pipe. Station should support the following methods:

Station(int sws, int rws, float propDrop) creates a Station with a sender window size sws and a receiver window size rws. I.e., this should set up a byte array senderBuffer of size 5*sws (5 is the frame size), and a byte array receiverBuffer of size 5*rws. propDrop is used to set up a field variable with the same name.
boolean isReady() returns whether the Station can receive a new frame to queue.
boolean send(int data) packages data as a 5 byte frame, writes it to the appropriate senderBuffer bytes.
byte[] nextTransmitFrame() chooses the next frame that will be sent from the Station based on the first applicable item below:
There is an acknowledgment frame that could be sent.
There is a frame in the sender window whose timer went off that has not yet been resent. Choose the oldest such frame that has not been resent. After choosing this frame reset its timer.
There is a frame in the sender window that has not yet been sent. Choose the oldest such frame and start a timer for it.
A non-frame frame as defined above.
After choosing the frame according to the above, nextTransmitFrame() generates a random number between 0 and 1. If it is less than propDrop, nextTransmitFrame() returns the non-frame frame; otherwise, it returns the frame chosen as above.
void receiveFrame(byte[] frame) if the frame is an acknowledgement frame, then this methods updates the variables associated with the sender window accordingly. If the frame is a data frame and its sequence number in the frame is within the receiver window, adds the frame to the Station's receiverBuffer and updates variables associated with the receiver window (including remembering enough, so that nextTransmitFrame() can send any necessary acknowledgment frames).
Pipe should support the following methods:

Pipe(int channelLength) creates a Pipe object that maintains a field byte[] channel of length 5*channelLength, representing the frame data that could be held in the Pipe.
byte[] addFrame(byte[] frame) shifts the data held in channel over by five bytes and writes frame to the first five bytes, the last five bytes, which have shifted out of the channel array, are returned from this method. So if before addFrame, channel looked like:
c_0, c_1, c_2, ..., c_{5*channelLength - 1}
after addFrame, channel would look like:
frame[0], frame[1], frame[2], frame[3], frame[4], c_0, ..., c_{5*channelLength - 6}
and the values c_{5*channelLength - 5}, c_{5*channelLength - 4}, c_{5*channelLength - 3}, c_{5*channelLength - 2}, c_{5*channelLength - 1} would be in the returned byte array.
float utilization() returns the number of frames currently in the pipe that are not non-frame frames divided by the channelLength.
void printContents() using System.out, prints, space separated, the bytes in the Pipe's channel. Bytes should by output as two digit hex numbers.
To conduct a simulation, the WindowSimulator instantiates two Station's, sender and receiver, with their sws and rws set according to the command line arguments. It also instantiates two Pipe's, senderPipe and receiverPipe. It initializes an int steps variable to 0, an int counter variable to 0, a boolean variable notDone to true, and a float sumUtilizations to 0. In a while(notDone) loop it then does the following:

Prints the word "Step" followed by the current value of step followed by a newline.
Prints "senderPipe" followed by a newline, followed by the result of calling printContents() on this Pipe, followed by a newline.
Prints "receiverPipe" followed by a newline, followed by the result of calling printContents() on this Pipe, followed by a newline.
Adds the average of the senderPipe and receiverPipe utilization to sumUtilizations.
Checks if there is still data to send ( counter < num_frames) and that isReady() is true. If so, it calls send(counter) and increments counter.
It calls the sender's nextTransmitFrame(), takes the byte array returned and calls senderPipe's addFrame method with it.
It takes the byte array returned from addFrame, and calls receiver's receiveFrame method using it.
The receiver's nextTransmitFrame() method is then called, and the byte array from this is used to call the receiverPipe's addFrame method.
The returned frame from this addFrame call is then sent to the sender's receiveFrame method. If this frame was an acknowledgement for the num_frames - 1th sent frame, then notDone is set to false to terminate the while loop.
If notDone is still false, steps should be incremented.
Once this loop completes, WindowSimulator should output the final value of steps and it should compute sumUtilizations/steps and output this as the average pipe utilization.

This completes the description of the simulator. Given that its working, you should now conduct the following experiment. Compare the average utilization sws=rws=channel_length=10 for different sender and receiver probabilities between 0 and 1 versus the average utilization sws=rws=1 and channel_length=10 for those same probabilities. Before you do the experiment come up with a hypothesis of what you might guess you'd see. Then conduct the experiment and analyze the results and draw conclusions. Put your write-up in a file Experiments.pdf that you include in your Hw2.zip file.
