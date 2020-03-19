package br.ce.wcaquino.servicos;



import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.builders.UsuBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.daos.LocacaoDaoFake;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoque;
import br.ce.wcaquino.exceptions.LocadoraExc;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.utils.DataUtils;
import buildermaster.BuilderMaster;

public class LocacaoServiceTest {

	private DataUtils data;
	
	private LocacaoService service;
	
	private SPCService spc;
	
	private LocacaoDao dao;
	
	private EmailService email;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup(){
		service = new LocacaoService();
		dao = Mockito.mock(LocacaoDao.class);
		service.setLocacaoDao(dao);
		spc = Mockito.mock(SPCService.class);
		service.setSPCService(spc);
		email = Mockito.mock(EmailService.class);
		service.setEmailService(email);
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(),Calendar.SATURDAY));

		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().comValor(5.0).agora());
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filme);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		//error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());

		//error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
		error.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComD(1));
	}
	
	@Test(expected = FilmeSemEstoque.class)
	public void naoDeveAlugaFilmeSemEstoque() throws Exception{
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().semEstoque().agora());		
		//acao
		service.alugarFilme(usuario, filme);
	}
	
	@Test
	public void naoDeveAlugarFilmeSemUsu() throws FilmeSemEstoque{
		//cenario
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());		
		//acao
		try {
			service.alugarFilme(null, filme);
			Assert.fail();
		} catch (LocadoraExc e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
		
	}
	

	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoque, LocadoraExc{
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		
		exception.expect(LocadoraExc.class);
		exception.expectMessage("Filme vazio");
		
		//acao
		service.alugarFilme(usuario, null);
		
	}
	
	
	@Test
	public void naoDeveDevolverNoDomingo() throws FilmeSemEstoque, LocadoraExc {
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(),Calendar.SATURDAY));
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificao
		
		boolean ehSegunda = DataUtils.verificarDiaSemana(resultado.getDataRetorno(), Calendar.MONDAY);
		//Assert.assertTrue(ehSegunda);	
		//Assert.assertThat(resultado.getDataRetorno(), new DiaSemanaMat(Calendar.MONDAY));
		//Assert.assertThat(resultado.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
		Assert.assertThat(resultado.getDataRetorno(), MatchersProprios.caiNumaSegunda());
		
	}
	
	@Test
	public void naoDeveAlugarParaNeg() throws FilmeSemEstoque {
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		Usuario usuario2 = UsuBuilder.umUsuario().comNome("Usuario 2").agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());	
		
		
		Mockito.when(spc.nomeNegtivado(usuario)).thenReturn(true);


		
		//acao
		try {
			service.alugarFilme(usuario, filme);
			//verificacao
			Assert.fail();
		} catch (LocadoraExc e) {
			Assert.assertThat(e.getMessage(), is("Usuario Negativado"));
		}
		
		Mockito.verify(spc).nomeNegtivado(usuario);
		
	}
	
	@Test
	public void deveEnviarEmailAoAtrasado() {
		//canario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Locacao> locacoes = 
				Arrays.asList(LocacaoBuilder.umLocacao()
						.comUsuario(usuario)
						.comDataRetorno(DataUtils.obterDataComDiferencaDias(-2))
						.agora());
		Mockito.when(dao.locacaoPendente()).thenReturn(locacoes);
		
		//acao
		service.notificaAtrasado();
		
		//verificacao
		Mockito.verify(email).notificaAtrasado(usuario);
	}


	
}



/*	@Test
	public void promo3FilmesPaga75Pct() throws FilmeSemEstoque, LocadoraExc {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filme = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), new Filme("Filme 3", 2, 4.0));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificacao
		Assert.assertThat(resultado.getValor(), is(11.0));
	}
	
	@Test
	public void promo4FilmesPaga50Pct() throws FilmeSemEstoque, LocadoraExc {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filme = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), 
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificacao
		Assert.assertThat(resultado.getValor(), is(13.0));
	}
	
	@Test
	public void promo5FilmesPaga25Pct() throws FilmeSemEstoque, LocadoraExc {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filme = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), 
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0), new Filme("Filme 5", 2, 4.0));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificacao
		Assert.assertThat(resultado.getValor(), is(14.0));
	}
	
	@Test
	public void promo6FilmesNaoPag() throws FilmeSemEstoque, LocadoraExc {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filme = Arrays.asList(new Filme("Filme 1", 2, 4.0), new Filme("Filme 2", 2, 4.0), 
				new Filme("Filme 3", 2, 4.0), new Filme("Filme 4", 2, 4.0), 
				new Filme("Filme 5", 2, 4.0), new Filme("Filme 6", 2, 4.0));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificacao
		Assert.assertThat(resultado.getValor(), is(14.0));
	}*/