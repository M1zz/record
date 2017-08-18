import java.io.*;
import java.net.*;

class UDPServer
{
	public static final int SERVERPORT = 6292;
	public static void main(String args[]) throws Exception
	{
		DatagramSocket serverSocket = new DatagramSocket(SERVERPORT);
		DatagramPacket receivePacket;
		try {
			int fileSize = 0;
			int ack = 0;
			DataOutputStream dos = null;
			
			long startTime = System.currentTimeMillis();
			//receive file loop
			while(true) {
				File file; 
				byte[] buf = new byte[1024];
				
				System.out.println("LISTENING...");
				
				//receive packet
				receivePacket = new DatagramPacket(buf, buf.length);
				serverSocket.receive(receivePacket);
				String str = new String(receivePacket.getData(), 0, receivePacket.getLength());
				
				if(str.equals("start")) {		//check "start"
					System.out.println("Start to get file");
					
					//receive file name
					serverSocket.receive(receivePacket);
					str = new String(receivePacket.getData(), 0, receivePacket.getLength());
					
					//make file to write
					file = new File(str);
					System.out.println("File Name : " + str);
					
					dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

					//receive file size
					serverSocket.receive(receivePacket);
					byte[] fileSizeBuf = new byte[4];
					System.arraycopy(buf, 0, fileSizeBuf, 0, fileSizeBuf.length);
					fileSize = byteArrayToInt(fileSizeBuf);					

				} else if(str.equals("end")) {	//check "end"
					long finishedTime = System.currentTimeMillis();
					System.out.println("Finished : "+(finishedTime-startTime)+"ms");
					dos.close();
					serverSocket.close();
					break;
				} else {
					byte[] sequence = new byte[4];
					byte[] fileBuffer = new byte[1020];

					System.arraycopy(buf, 0, sequence, 0, sequence.length); //get sequence number from received Bytes
					System.arraycopy(buf, sequence.length, fileBuffer, 0, fileBuffer.length); // get file data
					
					//check sequence number and send right ack
					if(byteArrayToInt(sequence) == ack) {
						ack++;
						dos.write(fileBuffer, 0, receivePacket.getLength());
					}
					System.out.println(((char) 27)+"[2J");
					System.out.println("seq : "+"("+(ack)+"/"+fileSize+")");
					DatagramPacket ackPacket = new DatagramPacket(intToByteArray(ack), 4, receivePacket.getAddress(), SERVERPORT);
                                       	serverSocket.send(ackPacket);

				}
			}
		} catch (SocketException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int byteArrayToInt(byte bytes[]) {
        	return ((((int)bytes[0] & 0xff) << 24) |
        	        (((int)bytes[1] & 0xff) << 16) |
        	        (((int)bytes[2] & 0xff) << 8) |
        	        (((int)bytes[3] & 0xff)));
    	}

	private static byte[] intToByteArray(int value) {
     	
	   	byte[] byteArray = new byte[4];
     	   	byteArray[0] = (byte)(value >> 24);
     	   	byteArray[1] = (byte)(value >> 16);
     	   	byteArray[2] = (byte)(value >> 8);
		byteArray[3] = (byte)(value);

        	return byteArray;
    	}
}
