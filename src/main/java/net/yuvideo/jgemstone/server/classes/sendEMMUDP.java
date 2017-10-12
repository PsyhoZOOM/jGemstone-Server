package net.yuvideo.jgemstone.server.classes;

/**
 * Created by zoom on 1/20/17.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class sendEMMUDP {

    int card;
    int paket;
    int chip;
    int start_d;
    int start_m;
    int start_y;
    int end_d;
    int end_m;
    int end_y;
    Calendar start = Calendar.getInstance();
    Calendar stop = Calendar.getInstance();
    private String HostName;
    private int Port;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress soAddress;


    public sendEMMUDP(String hostname, int port) {
        try {
            this.Port = port;
            this.HostName = hostname;
            this.soAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException ex) {
            Logger.getLogger(sendEMMUDP.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    static byte[] packN(int value) {
        //	byte[] bytes = ByteBuffer.allocate(4).putInt(new Integer(value)).array();
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte[] bytes = bb.order(ByteOrder.BIG_ENDIAN).putInt(value).array();
        for (int i = 0; i < bytes.length; i++) {
            //System.out.println(bytes[i]);
        }

        return bytes;
    }

    static byte[] packn(int data) {

        String dataS = String.valueOf(data);
        String encodedS = DatatypeConverter.printShort(Short.valueOf(dataS));
        byte[] bytes = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) data).array();

        for (int i = 0; i < bytes.length; i++) {

        }

        return bytes;

    }

    static byte[] packC(int data) {
        byte[] byteData;

        String encodedChar;
        encodedChar = DatatypeConverter.printInt(data);
        int parseInt = DatatypeConverter.parseInt(String.valueOf(data));
        byteData = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putChar((char) data).array();


        return byteData;
    }

    public void send(int card, LocalDate date_s, LocalDate date_e, int paket,
                     int chip) {

        try {

            start_d = date_s.getDayOfMonth();
            start_m = date_s.getMonthValue();
            start_y = date_s.getYear();

            end_d = date_e.getDayOfMonth();
            end_m = date_e.getMonthValue();
            end_y = date_e.getYear();


            //System.out.println(String.format("%d%d%d%d%d%d%d%d%d",
            //        card, paket, start_d, start_m, start_y, end_d, end_m, end_y, chip));


            byte[] cardS = packN(card);
            byte[] paketNo = packC(paket);
            byte[] cardSD = packC(start_d * 1);
            byte[] cardSM = packC(start_m * 1);
            byte[] cardSY = packn(start_y * 1);
            byte[] cardED = packC(end_d * 1);
            byte[] cardEM = packC(end_m * 1);
            byte[] cardEY = packn(end_y * 1);
            byte[] chipNo = packn(chip);


            byte[] packetSend = new byte[16];

            packetSend[0] = (byte) (cardS[0] & 0xff);
            packetSend[1] = (byte) (cardS[1] & 0xff);
            packetSend[2] = (byte) (cardS[2] & 0xff);
            packetSend[3] = (byte) (cardS[3] & 0xff);
            packetSend[4] = (byte) (paketNo[1] & 0xff);
            packetSend[5] = (byte) (cardSD[1] & 0xff);
            packetSend[6] = (byte) (cardSM[1] & 0xff);
            packetSend[7] = (byte) (cardSY[0] & 0xff);
            packetSend[8] = (byte) (cardSY[1] & 0xff);
            packetSend[9] = (byte) (cardED[1] & 0xff);
            packetSend[10] = (byte) (cardEM[1] & 0xff);
            packetSend[11] = (byte) (cardEY[0] & 0xff);
            packetSend[12] = (byte) (cardEY[1] & 0xff);
            packetSend[13] = (byte) (chipNo[0] & 0xff);
            packetSend[14] = (byte) (chipNo[1] & 0xff);

            int SixteenByte = 0;
            for (int i = 0; i < packetSend.length; i++) {
                SixteenByte += packetSend[i] & 0xff;
                //System.out.println(packetSend[i]);

            }


            packetSend[15] = (byte) (packC(SixteenByte)[1] & 0xff);


            packet = new DatagramPacket(packetSend, packetSend.length, soAddress, Port);
            socket = new DatagramSocket();

            socket.send(packet);
            socket.close();
        } catch (UnknownHostException ex) {
            Logger.getLogger(sendEMMUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(sendEMMUDP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


}
