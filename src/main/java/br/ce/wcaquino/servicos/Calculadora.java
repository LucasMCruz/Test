package br.ce.wcaquino.servicos;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroExc;

public class Calculadora {

	public int somar(int a, int b) {
		// TODO Auto-generated method stub
		return a + b;
	}

	public int subtrair(int a, int b) {
		// TODO Auto-generated method stub
		return a - b;
	}

	public int dividir(int a, int b) throws NaoPodeDividirPorZeroExc {
		if(b == 0 || a == 0) {
			throw new NaoPodeDividirPorZeroExc();
		}
		// TODO Auto-generated method stub
		return a / b;
	}
	


}
