package fi.otavanopisto.kuntaapi.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.Assert.fail;

public abstract class AbstractTest {
  
  protected int getHttpPort() {
    return Integer.parseInt(System.getProperty("it.port.http"));
  }

  protected String getHost() {
    return System.getProperty("it.host");
  }
  
  protected int getWireMockPort() {
    return getHttpPort() + 1;
  }
  
  protected String getWireMockBasePath() {
    return String.format("http://%s:%d", getHost(), getWireMockPort());
  }

  protected String getApiBasePath() {
    return String.format("http://%s:%d/v1", getHost(), getHttpPort());
  }
  
  protected long insertOrganizationSetting(String organizationId, String key, String value) {
    return executeInsert("insert into OrganizationSetting (settingKey, organizationKuntaApiId, value) values (?, ?, ?)", key, organizationId, value);
  }

  protected long insertSystemSetting(String key, String value) {
    return executeInsert("insert into SystemSetting (settingKey, value) values (?, ?)", key, value);
  }
  
  protected void deleteOrganizationSetting(String organizationId, String key) {
    executeDelete("delete from OrganizationSetting where settingKey = ? and organizationKuntaApiId = ?", key, organizationId);
  }

  protected void deleteSystemSetting(String key) {
    executeDelete("delete from SystemSetting where settingKey = ?", key);
  }

  protected long createIdentifier(String kuntaApiId, String source, String sourceId, String type) {
    return executeInsert("insert into Identifier (kuntaApiId, source, sourceId, type) values (?, ?, ?, ?)", 
      kuntaApiId, source, sourceId, type);
  }
  
  protected long executeInsert(String sql, Object... params) {
    try {
      Connection connection = getConnection();
      try {
        connection.setAutoCommit(true);
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
          for (int i = 0, l = params.length; i < l; i++) {
            statement.setObject(i + 1, params[i]);
          }
  
          statement.execute();
          
          try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
              return generatedKeys.getLong(1);
            }
          }
        } finally {
          statement.close();
        }
      } finally {
        connection.close();
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
    return -1;
  }
  
  protected void executeDelete(String sql, Object... params) {
    try {
      Connection connection = getConnection();
      try {
        connection.setAutoCommit(true);
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
          for (int i = 0, l = params.length; i < l; i++) {
            statement.setObject(i + 1, params[i]);
          }
  
          statement.execute();
        } finally {
          statement.close();
        }
      } finally {
        connection.close();
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  protected Connection getConnection() throws Exception {
    String username = System.getProperty("it.jdbc.username");
    String password = System.getProperty("it.jdbc.password");
    String url = System.getProperty("it.jdbc.url");
    Class.forName(System.getProperty("it.jdbc.driver")).newInstance();

    return DriverManager.getConnection(url, username, password);
  }
  
}
