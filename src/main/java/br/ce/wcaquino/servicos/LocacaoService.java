package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoque;
import br.ce.wcaquino.exceptions.LocadoraExc;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	private LocacaoDao dao;
	
	private SPCService spc;
	
	private EmailService email;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filme) throws FilmeSemEstoque, LocadoraExc {
		if(usuario == null) {
			throw new LocadoraExc("Usuario vazio");
		}
		
		if(filme == null || filme.isEmpty()) {
			throw new LocadoraExc("Filme vazio");
		}
		
		for (Filme filmes:filme) {
		
			if( filmes.getEstoque() == 0) {
				throw new FilmeSemEstoque();
			}
		}
		
		if(spc.nomeNegtivado(usuario)) {
			throw new LocadoraExc("Usuario Negativado");
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilme(filme);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		Double valorTotal = 0d;
		for (int i = 0; i < filme.size(); i++) {
			Filme filmes  = filme.get(i);
			Double valorFilme = filmes.getPrecoLocacao();
			if (i == 2) {
				valorFilme = valorFilme * 0.75;
			}
			if (i == 3) {
				valorFilme = valorFilme * 0.50;
			}
			if (i == 4) {
				valorFilme = valorFilme * 0.25;
			}
			if (i == 5) {
				valorFilme = valorFilme * 0;
			}
			
			valorTotal += valorFilme;
		}
		locacao.setValor(valorTotal);
		
		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
			
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		return locacao;
	}

	public void notificaAtrasado() {
		List<Locacao> locacoes = dao.locacaoPendente();
		for(Locacao locacao : locacoes) {
			email.notificaAtrasado(locacao.getUsuario());
			
		}
	
		
	}
	public void setLocacaoDao(LocacaoDao dao) {
		this.dao = dao;
	}
	
	public void setSPCService(SPCService spc) {
		this.spc = spc;
	}
	
	public void setEmailService(EmailService email) {
		this.email = email;
	}
	
}