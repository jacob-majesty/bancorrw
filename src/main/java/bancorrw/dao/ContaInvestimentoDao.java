package bancorrw.dao;

import bancorrw.cliente.Cliente;
import bancorrw.conta.ContaInvestimento;
import java.util.List;

public interface ContaInvestimentoDao extends Dao<ContaInvestimento> {

    public List<ContaInvestimento> getContasInvestimentoByCliente(Cliente cliente) throws Exception;

}