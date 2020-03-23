package br.ce.wcaquino.servicos;



import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.utils.DataUtils;


@RunWith(PowerMockRunner.class)
@PrepareForTest(LocacaoService.class)
public class LocacaoServiceTest_PowerMock {

	
	@InjectMocks
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
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {

		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().comValor(5.0).agora());
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(28, 4, 2017));
		
		
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filme);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), CoreMatchers.is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
		//error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		//error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());
		//error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
		//error.checkThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComD(1));
	}
	
	
	@Test
	public void naoDeveDevolverNoDomingo() throws Exception {
		//cenario
		Usuario usuario = UsuBuilder.umUsuario().agora();
		List<Filme> filme = Arrays.asList(FilmeBuilder.umFilme().agora());	
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(29, 4, 2017));
		
		
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificao
		Assert.assertThat(resultado.getDataRetorno(), MatchersProprios.caiNumaSegunda());
		
		
	}
	
}