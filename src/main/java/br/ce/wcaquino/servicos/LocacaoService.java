package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;
import static org.mockito.Matchers.booleanThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoque;
import br.ce.wcaquino.exceptions.LocadoraExc;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {
	
	@InjectMocks
	private LocacaoService service;
	
	@Mock
	private LocacaoDao dao;
	
	@Mock
	private SPCService spc;
	
	@Mock
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
		
		boolean negativado;
		
		try {
			negativado = spc.nomeNegtivado(usuario);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new LocadoraExc("Problemas com SPC, tente novamente");
		}
		if(negativado) {
			throw new LocadoraExc("Usuario Negativado");
		}
		
		Locacao locacao = new Locacao();
		locacao.setFilme(filme);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(obterData());
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
		Date dataEntrega = obterData();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if(DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
			
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		return locacao;
	}

	protected Date obterData() {
		return new Date();
	}

	public void notificaAtrasado() {
		List<Locacao> locacoes = dao.locacaoPendente();
		for(Locacao locacao : locacoes) {
			if(locacao.getDataRetorno().before(obterData())){
					email.notificaAtrasado(locacao.getUsuario());
			}
		}
	
		
	}
	
	public void prorrogarLocacao(Locacao locacao, int dias) {
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilme(locacao.getFilme());
		novaLocacao.setDataLocacao(obterData());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor()*dias);
		dao.salvar(novaLocacao);
		
	}



	
}