package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

enum tiposErro {
    // TODO FAZER DEPOIS
    // TODO FAZER Será valorizado o facto de o código das aplicações desenvolvidas ter sido estruturado
    // TODO servidor que recebe prepare fica a espera
    //de uma forma modular, com uma separação clara entre lógica de funcionamento,
    //lógica de comunicação e interface do utilizador, podendo esta ser em modo texto ou
    //gráfico conforme já mencionado
}

public class Servidor {
    static ArrayList<Thread> allThreads;
    static ArrayList<Informacoes> listaServidores;
    static ArrayList<ObjectOutputStream> listaOos;
    static MulticastSocket ms;
    static ServerSocket ss;
    static ConnDB connDB;
    static InetAddress ipgroup;
    static SocketAddress sa;
    static NetworkInterface ni;
    static String dBName;
    static int portServer;
    static int portClients;
    static String ipServer;
    static final int portServers = 4004;
    static final String MULTICAST_IP = "239.39.39.39";
    static AtomicInteger ligacoesTCP;
    static AtomicBoolean disponivel;
    static AtomicBoolean threadCorre;
    static AtomicInteger tentativas;

    public Servidor(String args0, String args1) {
        portClients = Integer.parseInt(args0);
        dBName = args1;
        allThreads = new ArrayList<>();
        listaServidores = new ArrayList<>();
        listaOos = new ArrayList<>();
        ligacoesTCP = new AtomicInteger(0);
        disponivel = new AtomicBoolean(true);
        threadCorre = new AtomicBoolean(true);
        tentativas = new AtomicInteger(0);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length != 2) {
            System.out.println("Argumentos inválidos {<PORT> <DBPATH>}");
            return;
        }
        new Servidor(args[0], args[1]);

        try {
            ms = new MulticastSocket(portServers);
            ipgroup = InetAddress.getByName(MULTICAST_IP);
            sa = new InetSocketAddress(ipgroup, portServers);
            ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            ms.joinGroup(sa, ni);

            ss = new ServerSocket(0);
            portServer = ss.getLocalPort();

            ipServer = InetAddress.getByName("localhost").getHostAddress();

            ListenHeartBeat lhb = new ListenHeartBeat(ms, listaServidores, threadCorre);
            lhb.start();
            allThreads.add(lhb);

            connDB = faseDeArranque(listaServidores);

            AtualizaServidor as = new AtualizaServidor(listaServidores, connDB, disponivel, listaOos, threadCorre);
            as.start();
            allThreads.add(as);

            HeartBeat hb = new HeartBeat(portServer, ipgroup, portServers, ms, ipServer, ligacoesTCP,
                    connDB.getVersao(), connDB.getDbName(), disponivel, threadCorre);
            hb.start();
            allThreads.add(hb);

            DatagramSocket ds = new DatagramSocket(portClients);
            ListenUDP lUDP = new ListenUDP(ds, listaServidores, threadCorre);
            lUDP.start();
            allThreads.add(lUDP);

            RemoveServidores rs = new RemoveServidores(listaServidores, threadCorre);
            rs.start();
            allThreads.add(rs);

            while (true) { // TODO TIRAR
                Socket sCli = ss.accept();
                ComunicaTCP ts = new ComunicaTCP(sCli, ligacoesTCP, dBName, disponivel, listaOos, threadCorre);
                ts.start();
                allThreads.add(ts);

               atualiza( "prepare",connDB.getVersao().get());
            }

        } catch (UnknownHostException e) {
            System.out.println("Desconhecido Host");

        } catch (IOException e) {
            System.out.println("Desconhecido");

        } finally {
            System.out.println("[INFO] A encerrar sessão...");
            threadCorre.getAndSet(false);
            for (Thread t : allThreads) {
                t.join();
            }
            ss.close();
            ms.leaveGroup(sa, ni);
            ms.close();
        }
    }

    private static ConnDB faseDeArranque(ArrayList<Informacoes> listaServidores) {
        ConnDB connDB;
        try {
            sleep(4000); //TODO passar para 30 segundos

            if (listaServidores.isEmpty()) {

                connDB = new ConnDB(dBName);
                connDB.criaTabelas();

            } else {
                connDB = new ConnDB(dBName);
                connDB.criaTabelas();
                System.out.println("SERVIDOR FASE DE ARRANQUE: " + connDB.getVersao());

                int valMaior = connDB.getVersao().get();
                int posMaior = -1;
                for (int i = 0; i < listaServidores.size(); i++) {
                    if (listaServidores.get(i).getVersaoBd() > valMaior) {
                        valMaior = listaServidores.get(i).getVersaoBd();
                        posMaior = i; // posicao do Servidor que tem maior versao
                    }
                }

                if (posMaior > -1) {
                    Socket servidorTemp = null;
                    try {
                        servidorTemp = new Socket("localhost", listaServidores.get(posMaior).getPorto());
                        System.out.println("Conectou-se por TCP ao Servidor [" + servidorTemp.getPort() + "]");

                        InputStream is = servidorTemp.getInputStream();
                        OutputStream os = servidorTemp.getOutputStream();
                        ObjectOutputStream oosTCP = new ObjectOutputStream(os);
                        ObjectInputStream oisTCP = new ObjectInputStream(is);
                        Msg msgTCP = new Msg();
                        msgTCP.setMsg("CloneBD");
                        oosTCP.writeUnshared(msgTCP);

                        FileOutputStream fos = new FileOutputStream(dBName);

                        Msg msg;
                        do {

                            try {
                                msg = (Msg) oisTCP.readObject();
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }

                            fos.write(msg.getMsgBuffer(), 0, msg.getMsgSize());

                        } while (!msg.isLastPacket());

                    } catch (IOException e) {
                        System.out.println("Não consegui aceder ao Socket do Servidor: " + servidorTemp.getPort());
                    } finally {
                        connDB.incrementaVersao();
                        System.out.println("Versao" + connDB.getVersao());
                        servidorTemp.close();
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        return connDB;
    }

    public static void atualiza(String msg, int valMaior) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = now.format(dateTimeFormatter);

            Informacoes info = new Informacoes(portServer, ipServer, ligacoesTCP.get(), currentTime, connDB.getVersao().get(), disponivel.get());
            info.setDbName(connDB.getDbName());
            if(msg!=null){
                switch (msg.toUpperCase()){
                    case "PREPARE"-> {
                        info.setMsgAtualiza("Prepare");
                        System.out.println("RECEBA");
                        DatagramSocket ds = new DatagramSocket(0);
                        info.setPortoUDPAtualiza(ds.getLocalPort());
                        info.setVersaoBdAtualiza(valMaior);
                        AtualizaUDP aUDP = new AtualizaUDP(ds, connDB,listaServidores,threadCorre,tentativas, valMaior);
                        aUDP.start();
                        allThreads.add(aUDP);
                    }
                    case "ABORT"->{
                        info.setMsgAtualiza("ABORT");
                        info.setVersaoBdAtualiza(valMaior);
                        //disponivel.getAndSet(true);
                    }
                }
            }
            // manda inteiro para não crashar // usamos Atomic Integer pois é independente de sincronização
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUnshared(info);

            byte[] myMessageBytes = baos.toByteArray();

            DatagramPacket dp = new DatagramPacket(
                    myMessageBytes, myMessageBytes.length,
                    ipgroup, portServers
            );
            ms.send(dp);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}