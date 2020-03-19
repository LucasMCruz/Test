package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.daos.LocacaoDao;
import br.ce.wcaquino.daos.LocacaoDaoFake;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoque;
import br.ce.wcaquino.exceptions.LocadoraExc;


@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

	private LocacaoService service;
	
	
	@Parameter
	public List<Filme> filme;
	
	@Parameter(value=1)
	public Double valorLocacao;
	
	@Parameter(value=2)
	public String cenario;
	
	private static Filme filme1 = FilmeBuilder.umFilme().agora();
	
	private static Filme filme2 = FilmeBuilder.umFilme().agora();
	
	private static Filme filme3 = FilmeBuilder.umFilme().agora();
	
	private static Filme filme4 = FilmeBuilder.umFilme().agora();
	
	private static Filme filme5 = FilmeBuilder.umFilme().agora();
	
	private static Filme filme6 = FilmeBuilder.umFilme().agora();
	
	
	
	@Before
	public void setup(){
		service = new LocacaoService();
		LocacaoDao dao = Mockito.mock(LocacaoDao.class);
		service.setLocacaoDao(dao);
		SPCService spc = Mockito.mock(SPCService.class);
		service.setSPCService(spc);
		
	}
	
	@Parameters(name="{2}")
	public static Collection<Object[]> getParametros(){
		return Arrays.asList(new Object[][] {
			{Arrays.asList(filme1, filme2, filme3), 11.0, "3 Filmes:25%"},
			{Arrays.asList(filme1, filme2, filme3, filme4), 13.0, "4 Filmes:50%"},
			{Arrays.asList(filme1, filme2, filme3, filme4, filme5), 14.0, "5 Filmes:75%"},
			{Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6), 14.0, "6 Filmes:100%"}
		});
	}
	
	
	@Test
	public void promo5FilmesPaga25Pct() throws FilmeSemEstoque, LocadoraExc {
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filme);
		
		//verificacao
		Assert.assertThat(resultado.getValor(), is(valorLocacao));
	}


}
