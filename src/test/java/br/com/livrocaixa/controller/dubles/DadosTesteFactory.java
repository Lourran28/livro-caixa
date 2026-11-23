package br.com.livrocaixa.controller.dubles;

import br.com.livrocaixa.model.Cliente;
import br.com.livrocaixa.model.FormaPagamento;
import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.model.Perfil;
import br.com.livrocaixa.model.TipoConsulta;
import br.com.livrocaixa.model.TipoSaida;

/**
 * Fábrica auxiliar para montar objetos de domínio válidos nos testes unitários.
 */
public final class DadosTesteFactory {

    private DadosTesteFactory() {
    }

    public static Perfil perfilAdmin() {
        Perfil p = new Perfil();
        p.setId(1);
        p.setDescricao("Administrador");
        return p;
    }

    public static Perfil perfilRecepcionista() {
        Perfil p = new Perfil();
        p.setId(2);
        p.setDescricao("Recepcionista");
        return p;
    }

    public static Funcionario admin() {
        Funcionario f = new Funcionario();
        f.setId(1);
        f.setNome("Dra. Josilene");
        f.setSenha("admin123");
        f.setPerfil(perfilAdmin());
        return f;
    }

    public static Funcionario recepcionista() {
        Funcionario f = new Funcionario();
        f.setId(2);
        f.setNome("Maria Recepcao");
        f.setSenha("recep123");
        f.setPerfil(perfilRecepcionista());
        return f;
    }

    public static Cliente clienteValido(Integer id, String nome, String cpf) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNome(nome);
        c.setCpf(cpf);
        return c;
    }

    public static TipoConsulta tipoConsultaPadrao() {
        TipoConsulta tc = new TipoConsulta();
        tc.setId(1);
        tc.setDescricao("Previdenciário");
        return tc;
    }

    public static TipoSaida tipoSaidaPadrao() {
        TipoSaida ts = new TipoSaida();
        ts.setId(1);
        ts.setDescricao("Material de Escritório");
        return ts;
    }

    public static FormaPagamento formaPix() {
        FormaPagamento fp = new FormaPagamento();
        fp.setId(2);
        fp.setDescricao("PIX");
        return fp;
    }

    public static FormaPagamento formaEspecie() {
        FormaPagamento fp = new FormaPagamento();
        fp.setId(1);
        fp.setDescricao("Espécie");
        return fp;
    }
}
