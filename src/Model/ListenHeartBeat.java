package Model;

import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class ListenHeartBeat extends Thread{
    private MulticastSocket ms;

    public ListenHeartBeat(MulticastSocket ms) {
        this.ms = ms;
    }

    @Override
    public void run() {
        while(true) {
            DatagramPacket dp = new DatagramPacket(new byte[256], 256);

            try {
                ms.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Msg msg = null;
            try {
                msg = (Msg) ois.readObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            System.out.println("String: " + msg.getMsg()+ "Porto: " + msg.getPortoTCP());
        }

    }
}



