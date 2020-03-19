package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Usuario;

public class UsuBuilder {
	
	private Usuario usuario;
	
	private UsuBuilder() {}
	
	public static UsuBuilder umUsuario() {
		UsuBuilder builder = new UsuBuilder();
		builder.usuario = new Usuario();
		builder.usuario.setNome("Usuario 1 ");
		return builder;
	}
	
	public UsuBuilder comNome(String nome) {
		usuario.setNome(nome);
		return this;
		
	}
	
	public Usuario agora() {
		return usuario;
	}

}
