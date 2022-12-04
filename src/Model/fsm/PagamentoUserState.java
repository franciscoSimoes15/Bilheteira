package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PagamentoUserState extends ClientAdapter {
    public PagamentoUserState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean consultaReservasPagas(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public boolean consultaReservasParaPagamento(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public boolean esperaPagamento(AtomicInteger pagamento) {
        return data.esperaPagamento(pagamento);
    }

    @Override
    public ClientState getState() {
        return ClientState.PAGAMENTO;
    }
}