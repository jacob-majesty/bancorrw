package bancorrw.cliente;

import bancorrw.conta.ContaCorrente;
import bancorrw.conta.ContaInvestimento;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cliente extends Pessoa {
    private ContaCorrente contaCorrente;
    private List<ContaInvestimento> contasInvestimento;
    private String cartaoCredito;

    public Cliente(int id, String nome, String cpf, LocalDate dataNascimento, String cartaoCredito) {
        super(id, nome, cpf, dataNascimento);
        this.cartaoCredito = cartaoCredito;
        this.contasInvestimento = new ArrayList<>();
    }

    public ContaCorrente getContaCorrente() {
        return contaCorrente;
    }

    public void setContaCorrente(ContaCorrente contaCorrente) throws Exception {
        this.contaCorrente = contaCorrente;
    }

   /*
    public void setContaCorrente(ContaCorrente contaCorrente) throws Exception {
        if (this.contaCorrente != null && this.contaCorrente.getSaldo() != 0) {
            String mensagemErro = String.format(Locale.US,
                    "Não pode modificar a conta corrente, pois saldo da original não está zerado. Para fazer isso primeiro zere o saldo da conta do cliente. Saldo=%.1f",
                    this.contaCorrente.getSaldo());
            throw new Exception(mensagemErro);
        }
        this.contaCorrente = contaCorrente;
    } */

    public List<ContaInvestimento> getContasInvestimento() {
        return contasInvestimento;
    }

    public void setContasInvestimento(List<ContaInvestimento> contasInvestimento) {
        this.contasInvestimento = contasInvestimento;
    }

    public void addContaInvestimento(ContaInvestimento contaInvestimento) {
        this.contasInvestimento.add(contaInvestimento);
    }

    public String getCartaoCredito() {
        return cartaoCredito;
    }

    public void setCartaoCredito(String cartaoCredito) {
        this.cartaoCredito = cartaoCredito;
    }

    /**
     * Calcula e retorna a soma dos saldos de todas as contas do cliente.
     * Inclui a conta corrente e todas as contas de investimento.
     * @return O saldo total do cliente.
     */
    public double getSaldoTotalCliente() {
        double saldoTotal = 0.0;

        // Adiciona o saldo da conta corrente, se ela existir.
        if (this.contaCorrente != null) {
            saldoTotal += this.contaCorrente.getSaldo();
        }

        // Soma os saldos de todas as contas de investimento.
        if (this.contasInvestimento != null) {
            for (ContaInvestimento contaInv : this.contasInvestimento) {
                saldoTotal += contaInv.getSaldo();
            }
        }
        return saldoTotal;
    }

}
