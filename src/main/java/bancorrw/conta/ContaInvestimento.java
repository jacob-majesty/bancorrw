package bancorrw.conta;

import bancorrw.cliente.Cliente;
import bancorrw.exception.DbIntegrityException;

import java.util.Locale;

public class ContaInvestimento extends Conta {
    private double taxaRemuneracaoInvestimento;
    private double montanteMinimo;
    private double depositoMinimo;

    public ContaInvestimento(double taxaRemuneracaoInvestimento, double montanteMinimo, double depositoMinimo, double saldo, long id, Cliente cliente) {
        super(id, cliente, saldo);
        this.taxaRemuneracaoInvestimento = taxaRemuneracaoInvestimento;
        this.montanteMinimo = montanteMinimo;
        this.depositoMinimo = depositoMinimo;

        cliente.addContaInvestimento(this);

        // Valida o saldo inicial conforme a regra de negócio
        if (saldo < montanteMinimo) {
            throw new IllegalArgumentException("Saldo não pode ser menor que montante mínimo.");
        }
    }

    // Métodos Getters e Setters
    public double getTaxaRemuneracaoInvestimento() {
        return taxaRemuneracaoInvestimento;
    }

    public void setTaxaRemuneracaoInvestimento(double taxaRemuneracaoInvestimento) {
        this.taxaRemuneracaoInvestimento = taxaRemuneracaoInvestimento;
    }

    public double getMontanteMinimo() {
        return montanteMinimo;
    }

    public void setMontanteMinimo(double montanteMinimo) {
        this.montanteMinimo = montanteMinimo;
    }

    public double getDepositoMinimo() {
        return depositoMinimo;
    }

    public void setDepositoMinimo(double depositoMinimo) {
        this.depositoMinimo = depositoMinimo;
    }

    // Implementação dos métodos da classe Conta
    @Override
    public void saca(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do saque deve ser maior que zero.");
        }

        double novoSaldo = getSaldo() - valor;
        if (novoSaldo < montanteMinimo) {
            String mensagemErro = String.format(Locale.US,
                    "Saldo insuficiente para saque. Valor Saque=%.1f Saldo=%.1f Montante Minimo=%.1f",
                    valor, getSaldo(), montanteMinimo);
            throw new DbIntegrityException(mensagemErro);
        }
        setSaldo(novoSaldo);
    }

    @Override
    public void deposita(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser maior que zero.");
        }

        if (valor < depositoMinimo) {
            String mensagemErro = String.format(Locale.US,
                    "Valor do depóstio não atingiu o mínimo. Valor Depósito=%.1f Depóstio Mínimo=%.1f",
                    valor, depositoMinimo);
            throw new IllegalArgumentException(mensagemErro);
        }
        setSaldo(getSaldo() + valor);
    }

    @Override
    public void aplicaJuros() {
        double juros = getSaldo() * taxaRemuneracaoInvestimento;
        setSaldo(getSaldo() + juros);
    }
}