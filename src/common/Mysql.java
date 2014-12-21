package common;

import java.sql.*;

public class Mysql {
	private Connection conn = null;
	private Statement enunciado;
	private static Mysql instancia;
	static String ip = "localhost";
	static String bd = "ragnarok_mro";
	static String login = "root";
	static String password = "24713401";
	static String url = "jdbc:mysql://" + ip + "/" + bd + "?useUnicode=true&characterEncoding=Big5";

	public synchronized static Mysql getInstancia() {
		if (instancia == null) {
			instancia = new Mysql();
		}
		return instancia;
	}
	public Mysql() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, login, password);
			enunciado = conn.createStatement();
			if (conn != null) {
				System.out.println("Connecting to database " + url + " ... Ok");
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.err.println("Error en");
		} catch (Exception ex) {
			System.err.println("Error" + ex.getMessage());
		}
	}
	public ResultSet hacerConsulta(String consulta) {
		ResultSet resultado = null;
		try {
			resultado = enunciado.executeQuery(consulta);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultado;
	}
	public Connection getConnection() {
		return conn;
	}

}