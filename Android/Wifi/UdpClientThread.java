package com.example.cjk.dc07;

import android.os.Environment;
import android.os.Message;

import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class UdpClientThread extends Thread{

    String dstAddress;
    int dstPort;
    String fileName, path;
    private boolean running;
    MainActivity.UdpClientHandler handler;

    DatagramSocket socket;
    InetAddress address;

    public static final int WINDOW_SIZE = 15;

    public UdpClientThread(String addr, int port, String fileNameForTransfer, MainActivity.UdpClientHandler handler) {
        super();
        dstAddress = addr;
        fileName = fileNameForTransfer;
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;
        dstPort = port;
        this.handler = handler;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state){
        handler.sendMessage(
                Message.obtain(handler,
                        MainActivity.UdpClientHandler.UPDATE_STATE, state));
    }

    //int and byte conversion (Big Endian)
    public byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }

    public int byteArrayToInt(byte bytes[]) {
        return ((((int)bytes[0] & 0xff) << 24) |
                (((int)bytes[1] & 0xff) << 16) |
                (((int)bytes[2] & 0xff) << 8) |
                (((int)bytes[3] & 0xff)));
    }

    //String to md5 hash value
    public String toMD5(String str) {
        String result = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteData = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0;i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
            result = "";
        }

        return result;
    }

    @Override
    public void run() {
        sendState("connecting...");
        running = true;

        try {
            int lastSent = 0; //seqeunce number of last sent packet
            int waitingAck = 0; //sequence number of last received ack
            int fileSize = 0;
            socket = new DatagramSocket(dstPort);
            address = InetAddress.getByName(dstAddress);

            //send "start"
            byte[] startMsg = "start".getBytes();
            DatagramPacket packet = new DatagramPacket(startMsg, startMsg.length, address, dstPort);
            socket.send(packet);

            //send File Name
            byte[] fileNameBuf = fileName.getBytes();
            packet = new DatagramPacket(fileNameBuf, fileNameBuf.length, address, dstPort);
            socket.send(packet);


            //List all packets
            ArrayList<DatagramPacket> packetsForSend = new ArrayList<DatagramPacket>();
            //ArrayList<DatagramPacket> md5PacketsForSend = new ArrayList<DatagramPacket>();

            //read file
            byte[] fileBuffer = new byte[1020];
            FileInputStream fis = new FileInputStream(path);

            for(int i = 0; (fis.read(fileBuffer, 0, fileBuffer.length)) != -1; i++) {
                //int to bytes and combine sequence and file
                byte[] sequenceBuffer = intToByteArray(i);
                byte[] sendBuffer = new byte[1024];
                System.arraycopy(sequenceBuffer, 0, sendBuffer, 0, sequenceBuffer.length);
                System.arraycopy(fileBuffer, 0, sendBuffer, sequenceBuffer.length, fileBuffer.length);

                packet = new DatagramPacket(sendBuffer, sendBuffer.length, address, dstPort);
                packetsForSend.add(packet);
            }

            //send file size
            fileSize = packetsForSend.size();
            byte[] fileSizeBuffer = intToByteArray(fileSize);

            packet = new DatagramPacket(fileSizeBuffer, fileSizeBuffer.length, address, dstPort);
            socket.send(packet);

            //send File
            while(true) {

                //sending loop for WINDOW_SIZE
                while (lastSent - waitingAck < WINDOW_SIZE && lastSent < fileSize) {
                    //send file
                    if(Math.random() > 0.2) { //packet loss setup
                        socket.send(packetsForSend.get(lastSent));
                        sendState("file sending : " + lastSent);
                    } else {
                        sendState("packet loss : " + lastSent);
                    }

                    lastSent++;
                }

                //stop and wait
                try {
                    byte[] receiveBuffer = new byte[4];
                    DatagramPacket ackPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.setSoTimeout(30);
                    socket.receive(ackPacket);

                    int receivedAck = byteArrayToInt(receiveBuffer);

                    //check for last ack
                    if (receivedAck == fileSize) {
                        //send "end"
                        byte[] endMsg = "end".getBytes();
                        packet = new DatagramPacket(endMsg, endMsg.length, address, dstPort);
                        socket.send(packet);
                        fis.close();

                        break;
                    }

                    waitingAck = Math.max(waitingAck, receivedAck);
                } catch (SocketTimeoutException e) {
                    sendState("ack " + waitingAck + " timeout");

                    for(int i = waitingAck; i < lastSent; i++) {
                        socket.send(packetsForSend.get(i));
                        sendState("file sending : " + i);

                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            sendState("SocketException");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            sendState("UnknownHostExeption");
        } catch (IOException e) {
            e.printStackTrace();
            sendState("IOException");

            StringBuffer a;
        } finally {
            if(socket != null){
                socket.close();
                handler.sendEmptyMessage(MainActivity.UdpClientHandler.UPDATE_END);
            }
        }

    }
}