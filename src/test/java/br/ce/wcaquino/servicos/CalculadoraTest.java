package br.ce.wcaquino.servicos;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroExc;
import br.ce.wcaquino.runners.ParalleloRunner;

@RunWith(ParalleloRunner.class)
public class CalculadoraTest {
	
	private Calculadora calc;
	
	@Before
	public void setup() {
		calc = new  Calculadora();
		System.out.println("iniciando");
	}
	
	@After
	public void tearDown() {
		System.out.println("finalizando");

	}

	@Test
	public void Somar() {
		int a = 5;
		int b = 3;
		
		
		int resultado = calc.somar(a, b);
		
		Assert.assertEquals(8, resultado);
	}
	
	@Test
	public void Sub() {
		int a = 7;
		int b = 4;
		
		
		int resultado = calc.subtrair(a, b);
		
		Assert.assertEquals(3, resultado);
	}
	
	@Test
	public void Div() throws NaoPodeDividirPorZeroExc {
		int a = 8;
		int b = 4;
		
		
		int resultado = calc.dividir(a, b);
		
		Assert.assertEquals(2, resultado);
	}
	
	@Test(expected = NaoPodeDividirPorZeroExc.class)
	public void DivZero() throws NaoPodeDividirPorZeroExc {
		int a = 7;
		int b = 0;
		
		
		int resultado = calc.dividir(a, b);
		
	}

}
