package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.Scanner;


public class Servidor {

    static final int portServers = 4004;
    static int portClients;
    static String DATASE_DIR = "./DataBase/";
    static String dBName; // TODO dbName+path
    static final String MULTICAST_IP = "239.39.39.39";

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Argumentos inválidos {<PORT> <DBPATH> -> <9021><D:\\uni\\3ano\\1semestre\\PD\\BilheteiraGit\\DataBase>}");
            return;
        }

        portClients = Integer.parseInt(args[0]);
        dBName = args[1];
        MulticastSocket ms;

        try {
            ConnDB connDB = new ConnDB(dBName);

            ms = new MulticastSocket(portServers);
            InetAddress ipgroup = InetAddress.getByName(MULTICAST_IP);
            SocketAddress sa = new InetSocketAddress(ipgroup, portServers);
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            ms.joinGroup(sa, ni);

            ThreadServer ts = new ThreadServer(ms, portClients);
            ts.start();

            System.out.println("Welcome to the chat!");
            Scanner sc = new Scanner(System.in);

            HeartBeat hb = new HeartBeat(ms);
            hb.start();

            while (true) {
                String msg = sc.nextLine();
                Msg myMessage = new Msg(msg);
                if(myMessage.getMsg().equals("exit"))
                    break;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(myMessage);
                byte[] myMessageBytes = baos.toByteArray();

                DatagramPacket dp = new DatagramPacket(
                        myMessageBytes, myMessageBytes.length,
                        ipgroup, portServers
                );

                ms.send(dp);
            }

            ms.leaveGroup(sa, ni);
            ms.close();
            ts.join();
            hb.join();

        } catch (UnknownHostException e) {
            System.out.println("Desconhecido Host");
        } catch (IOException e) {
            System.out.println("Desconhecido");
        } catch (InterruptedException e) {
            System.out.println("InterrupcaoException");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}

