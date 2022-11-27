package Model;

import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

public class Msg implements Serializable {
    @Serial
    static final long serialVersionUID = 1L;
    private String msg;
    private int portoServer;
    private String ip;
    private boolean lastPacket;
    private int ligacoesTCP;
    private byte[] msgBuffer;
    private int msgSize;
    private  ArrayList<Socket> listaClientes;
    public Msg(){ }

    public Msg(String ipServer , Integer portoServer){
        this.ip = ipServer;
        this.portoServer = portoServer;
    }
    public ArrayList<Socket> getListaClientes() {
        return listaClientes;
    }

    public void setListaClientes(ArrayList<Socket> listaClientes) {
        this.listaClientes = listaClientes;
    }
    public byte[] getMsgBuffer() {
        return msgBuffer;
    }

    public void setMsgBuffer(byte[] msgBuffer) {
        this.msgBuffer = msgBuffer;
    }

    public int getMsgSize() {
        return msgSize;
    }

    public void setMsgSize(int msgSize) {
        this.msgSize = msgSize;
    }

    public void setLastPacket(boolean lastPacket) {
        this.lastPacket = lastPacket;
    }
    public boolean isLastPacket() {
        return lastPacket;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setPortoServer(int portoServer) {
        this.portoServer = portoServer;
    }
    public Integer getPortoServer() {
        return this.portoServer;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        return ip;
    }

    public int getLigacoesTCP() {
        return ligacoesTCP;
    }

    public void setLigacoesTCP(int ligacoesTCP) {
        this.ligacoesTCP = ligacoesTCP;
    }

    @Override
    public String toString() {
        return "porto: " + portoServer + " ip: " + ip + " ligacoesTCP: " + ligacoesTCP ;
    }
}
