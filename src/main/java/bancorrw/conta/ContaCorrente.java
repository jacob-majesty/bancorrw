package bancorrw.conta;

import bancorrw.cliente.Cliente;
import bancorrw.exception.DbIntegrityException;

import java.util.Locale;

public class ContaCorrente extends Conta {
    private double limite;
    private double taxaJurosLimite;


    public ContaCorrente(double limite, double taxaJurosLimite, long id, Cliente cliente, double saldo) throws Exception {
        super(id, cliente, saldo);
        this.limite = limite;
        this.taxaJurosLimite = taxaJurosLimite;
            cliente.setContaCorrente(this);
    }

    public double getLimite() {
        return limite;
    }

    public void setLimite(double limite) {
        this.limite = limite;
    }

    public double getTaxaJurosLimite() {
        return taxaJurosLimite;
    }

    public void setTaxaJurosLimite(double taxaJurosLimite) {
        this.taxaJurosLimite = taxaJurosLimite;
    }

    @Override
    public void saca(double valor) throws Exception {
        if (valor <= 0) {
            String mensagemErro = String.format(Locale.US, "Valor do saque não pode ser negativo ou zero. Valor=%.1f", valor);
            throw new IllegalArgumentException(mensagemErro);
        }

        double totalDisponivel = this.getSaldo() + this.getLimite();

        if (valor > totalDisponivel) {
            String mensagemErro = String.format(Locale.US, "Saldo insuficiente na conta.\n" +
                    "Valor saque=%.1f\n" +
                    "Saldo=%.1f\n" +
                    "Limite=%.1f", valor, this.getSaldo(), this.getLimite());
            throw new DbIntegrityException(mensagemErro);
        }

        this.setSaldo(this.getSaldo() - valor);
    }

    @Override
    public void deposita(double valor) {
        if (valor <= 0) {
            String mensagemErro = String.format(Locale.US, "Valor do depósito não pode ser negativo ou zero. Valor=%.1f", valor);
            throw new IllegalArgumentException(mensagemErro);
        }
        this.setSaldo(this.getSaldo() + valor);
    }

    @Override
    public void aplicaJuros() {
        if (this.getSaldo() < 0) {
            double juros = this.getSaldo() * this.getTaxaJurosLimite();
            this.setSaldo(this.getSaldo() + juros);
        }
    }
}