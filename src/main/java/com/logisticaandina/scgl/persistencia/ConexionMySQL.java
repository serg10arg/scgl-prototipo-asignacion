package com.logisticaandina.scgl.persistencia;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexiones JDBC a MySQL. Lee las credenciales de
 * config.properties. No usa frameworks de alto nivel (RNF9): solo
 * el driver Connector/J (JDBC), que se autoregistra por SPI.
 */
public final class ConexionMySQL {

    private static String url;
    private static String usuario;
    private static String password;

    static { cargarConfiguracion(); }

    private ConexionMySQL() { }

    private static void cargarConfiguracion() {
        try (InputStream in = ConexionMySQL.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties p = new Properties();
            if (in != null) {
                p.load(in);
                url = p.getProperty("db.url");
                usuario = p.getProperty("db.usuario");
                password = p.getProperty("db.password");
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar config.properties", e);
        }
    }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }
}
