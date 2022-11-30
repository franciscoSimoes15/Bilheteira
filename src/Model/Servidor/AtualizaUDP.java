package Model.Servidor;

import utils.Msg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class AtualizaUDP extends Thread{
    DatagramSocket ds;
    ConnDB connDB;
    ArrayList<Informacoes> listaServidores;
    AtomicBoolean threadCorre;
    AtomicInteger tentativas;
    int valMaior;

    public AtualizaUDP(DatagramSocket ds, ConnDB connDB, ArrayList<Informacoes> listaServidores, AtomicBoolean threadCorre, AtomicInteger tentativas, int valMaior) {
        this.ds = ds;
        this.connDB = connDB;
        this.listaServidores = listaServidores;
        this.threadCorre = threadCorre;
        this.tentativas = tentativas;
        this.valMaior = valMaior;
    }

    @Override
    public void run() {
        System.out.println("para de ser burro xico");
        DatagramPacket dp = new DatagramPacket(new byte[255],255);
        System.out.println("CABRRO");
        synchronized (listaServidores){
            Iterator<Informacoes> it = listaServidores.iterator();
            System.out.println("ItAntes: "+ it.hasNext());
            while(it.hasNext() && threadCorre.get()){
                it.next();
                System.out.println("It: "+ it.hasNext());
                try {
                    //ds.setSoTimeout(5000);
                    ds.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);

                    Msg msg = (Msg) ois.readObject();
                    System.out.println("It: "+ msg.getVersaoBdAtualizada());
                    System.out.println("Versao [" + msg.getVersaoBdAtualizada()+"]");
                    break;
                } catch (SocketTimeoutException e) {
                    if(tentativas.get() < 1){
                        Servidor.atualiza("Prepare", valMaior);
                        tentativas.getAndIncrement();
                        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");
                        return;
                    }else{
                        System.out.println("!QUE TAS AQUI A FAZER");
                        Servidor.atualiza("Abort", valMaior);
                        return;
                    }
                    // Se for a segunda tentativa manda Abort
                }catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //System.out.println("!GANDA TONE ATUALIZAUDP");
            }
            System.out.println("!GANDA TONE ATUALIZAUDP");
            Servidor.atualiza("Commit", valMaior);
            System.out.println("VERSAO ANTES" + connDB.getVersao());
            connDB.setVersaoDB(valMaior);
            System.out.println("VERSAO DEPOIS" + connDB.getVersao());

            System.out.println("Recebi de todos");

        }

        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");

    }
}