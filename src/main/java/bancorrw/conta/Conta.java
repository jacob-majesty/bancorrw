package bancorrw.conta;

import bancorrw.cliente.Cliente;

public abstract class Conta {
    protected long id;
    protected Cliente cliente;
    protected double saldo;

    // Construtor completo, usado pelo DAO para recuperar dados do banco de dados.
    public Conta(long id, Cliente cliente, double saldo) {
        this.id = id;
        this.cliente = cliente;
        this.saldo = saldo;
    }

    // Construtor simplificado, usado para criar novas contas no código.
    public Conta(Cliente cliente, double saldo) {
        this(-1, cliente, saldo);
    }

    // Métodos Getters e Setters (conforme o seu código original)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    // Métodos abstratos que as subclasses devem implementar
    public abstract void saca(double valor) throws Exception;
    public abstract void deposita(double valor) throws Exception;
    public abstract void aplicaJuros();
}