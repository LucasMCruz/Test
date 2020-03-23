package br.ce.wcaquino.servicos;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class CalculadoraMockTest {
	
	@Mock
	private Calculadora calcMock;
	
	@Spy
	private Calculadora calcSpy;
	
	@Spy
	private EmailService email;
	
	
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		
	}
	
	@Test
	public void diferencaMockSpy() {
		Mockito.when(calcMock.somar(1, 2)).thenReturn(8);
		Mockito.when(calcSpy.somar(1, 2)).thenReturn(8);
		Mockito.doReturn(5).when(calcSpy).somar(1, 2);
		Mockito.doNothing().when(calcSpy).imprimir();
		
		System.out.println(calcMock.somar(1, 5));
		System.out.println(calcSpy.somar(1, 5));
		System.out.println("");
	}
	
	@Test
	public void teste() {
		Calculadora calc = Mockito.mock(Calculadora.class);
		
		ArgumentCaptor<Integer> argCapt = ArgumentCaptor.forClass(Integer.class);
		Mockito.when(calc.somar(argCapt.capture(), argCapt.capture())).thenReturn(5);

		Assert.assertEquals(5, calc.somar(1, 100000));
		System.out.println(argCapt.getAllValues());
	}

}
