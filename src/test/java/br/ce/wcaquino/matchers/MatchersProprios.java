package br.ce.wcaquino.matchers;

import java.util.Calendar;

public class MatchersProprios {
	public static DiaSemanaMat caiEm(Integer diaSemana) {
		return new DiaSemanaMat(diaSemana);
	}
	
	
	public static DiaSemanaMat caiNumaSegunda() {
		return new DiaSemanaMat(Calendar.MONDAY);
	}
	
	public static DataDiferencaDias ehHojeComD(Integer qtdDias) {
		return new DataDiferencaDias(qtdDias);
	}
	
	public static DataDiferencaDias ehHoje() {
		return new DataDiferencaDias(0);
	}
}
