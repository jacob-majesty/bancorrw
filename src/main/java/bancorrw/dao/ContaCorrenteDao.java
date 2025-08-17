
package bancorrw.dao;

import bancorrw.cliente.Cliente;
import bancorrw.conta.ContaCorrente;


public interface ContaCorrenteDao extends Dao<ContaCorrente>{

    public ContaCorrente getContaCorrenteByCliente(Cliente cliente) throws Exception;
    
}
