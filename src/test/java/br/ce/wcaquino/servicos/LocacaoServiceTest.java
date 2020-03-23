package br.ce.wcaquino.servicos;



import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.builders.UsuBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoque;
import br.ce.wcaquino.exceptions.LocadoraExc;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.runners.ParalleloRunner;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(ParalleloRunner.class)
public class LocacaoServiceTest {

	
	@InjectMocks @Spy
	private LocacaoService service;
	
	@Mock
	private SPCService spc;
	
	@Mock
	private LocacaoDao dao;
	
	@Mock
	private EmailService email;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		System.out.println("iniciando");

	}
	
	@After
	public void tearDown() {
		System.out.println("finnanzlinaod");

	}
	
	@Test
	public void deveAlugarFilme() throws Exception {

		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().comValor(5.0).agora());
	
		Mockito.doReturn(DataUtils.obterData(28, 4, 2017)).when(service).obterData();
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filme);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
		//error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		//error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());
		//error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
		//error.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComD(1));
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
	public void naoDeveDevolverNoDomingo() throws Exception {
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());	

		Mockito.doReturn(DataUtils.obterData(29, 4, 2017)).when(service).obterData();
	
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificao
		Assert.assertThat(resultado.getDataRetorno(), MatchersProprios.caiNumaSegunda());
		
		
	}
	
	@Test
	public void naoDeveAlugarParaNeg() throws Exception {
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		Usuario usuario2 = UsuBuilder.umUsuario().comNome("Usuario 2").agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());	
		
		
		Mockito.when(spc.nomeNegtivado(Mockito.any(Usuario.class))).thenReturn(true);


		
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
		Usuario usuario2 = UsuBuilder.umUsuario().comNome("Usuario 2").agora();
		Usuario usuario3 = UsuBuilder.umUsuario().comNome("outro atrasado").agora();
		List<Locacao> locacoes = 
				Arrays.asList(
						LocacaoBuilder.umLocacao().comUsuario(usuario).atrasado().agora(),
						LocacaoBuilder.umLocacao().comUsuario(usuario2).agora(),
						LocacaoBuilder.umLocacao().comUsuario(usuario3).atrasado().agora(),
						LocacaoBuilder.umLocacao().comUsuario(usuario3).atrasado().agora());
		Mockito.when(dao.locacaoPendente()).thenReturn(locacoes);
		
		//acao
		service.notificaAtrasado();
		
		//verificacao
		Mockito.verify(email, Mockito.times(3)).notificaAtrasado(Mockito.any(Usuario.class));
		Mockito.verify(email).notificaAtrasado(usuario);
		Mockito.verify(email, Mockito.atLeastOnce()).notificaAtrasado(usuario3);
		Mockito.verify(email, never()).notificaAtrasado(usuario2);
		Mockito.verifyNoMoreInteractions(email);
	}
	
	@Test
	public void deveTratarErroSPC() throws Exception {
		//cenariol
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());		
;
		Mockito.when(spc.nomeNegtivado(usuario)).thenThrow(new Exception("Falha catastrofica"));
		
		
		exception.expect(LocadoraExc.class);
		exception.expectMessage("Problemas com SPC, tente novamente");
		
		//acao
		service.alugarFilme(usuario, filme);
	}
	
	@Test
	public void deveProrrogar() {
		//cenario
		Locacao locacao = LocacaoBuilder.umLocacao().agora();
		
		
		//acao
		service.prorrogarLocacao(locacao, 3);
		
		//verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);
		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.ehHojeComD(3));

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