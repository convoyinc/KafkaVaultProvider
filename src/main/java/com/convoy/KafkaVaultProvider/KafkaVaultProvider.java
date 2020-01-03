package com.convoy.KafkaVaultProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;

import org.apache.kafka.common.config.ConfigData;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.provider.ConfigProvider;
import org.json.JSONObject;

public class KafkaVaultProvider implements ConfigProvider {

  // public for UnitTest context setup to reference from
  public static final String CONSTANT_X_VAULT_TOKEN = "x_vault_token";
  public static final String CONSTANT_REQUEST_URI_BASE = "request_uri_base";
  public static final String CONSTANT_SECRET_DIRECTORY = "secret_directory";
  public static final String ENV_VAR_VAULT_ADDR = System.getenv("VAULT_ADDR");
  public static final String X_VAULT_TOKEN = System.getenv("VAULT_TOKEN");

  final static Logger LOGGER = Logger.getLogger(KafkaVaultProvider.class.getSimpleName());
  // For testing purpose in localhost
  static final String CONSTANT_LOCALHOST_URI = "http://127.0.0.1:8200";

  String apiVersion = "v1";
  ThreadLocalProperties localProperties = new ThreadLocalProperties();

  /**
   * Retrieves the data that exist in <request_uri_base>/<secret_directory>
   *
   * @param path Not being used
   * @return the secret value of data
   */
  @Override
  public ConfigData get(String path) {
    Map<String, String> data = new HashMap<>();
    try {
      data = getVaultData(path);
      return new ConfigData(data);
    } catch (ConfigurationException | IOException e) {
      throw new ConfigException(e.getMessage());
    }
  }

  /**
   * Retrieves the data that matches a given set of secret keys.
   *
   * @param url  not being used
   * @param keys the set of keys for the vault lookup
   * @return the secret values of data
   */
  @Override
  public ConfigData get(String path, Set<String> keys) {
    Map<String, String> configData = new HashMap<>();
    try {
      Map<String, String> vaultData = getVaultData(path);
      for (String k : keys) {
        String val = vaultData.get(k);
        if (val != null) {
          configData.put(k, val);
        }
      }
      return new ConfigData(configData);
    } catch (ConfigurationException | IOException e) {
      throw new ConfigException(e.getMessage());
    }
  }

  /**
   * Retrieves all the data that exist in <request_uri_base>/<secret_directory>
   * 
   * @throws ParseException
   */
  public Map<String, String> getVaultData(String path)
      throws MalformedURLException, ConfigurationException, IOException {
    if (path.isEmpty()) {
      throw new ConfigException(KafkaVaultProvider.class.getSimpleName()
          + ": Missing or empty secret directory. Need to pass a valid relative URL path to your secrets");
    }
    // Making HTTP GET request to Vault server to retrieve secrets
    URL vaultRequestUrl = buildVaultRequestUrl(localProperties.getLocalProperty(CONSTANT_REQUEST_URI_BASE), apiVersion,
        path);
    String responseStr = VaultApiCaller.get(vaultRequestUrl, localProperties.getLocalProperty(CONSTANT_X_VAULT_TOKEN));
    JSONObject respJson = new JSONObject(responseStr);
    JSONObject dataPayload = respJson.getJSONObject("data");

    Map<String, String> dataMap = new HashMap<String, String>();
    // Add all string type fields in response data to config data map
    for (String key : dataPayload.keySet()) {
      Object value = dataPayload.get(key);
      if (value != null && value instanceof String) {
        dataMap.put(key, (String) value);
      }
    }

    return dataMap;
  }

  public static void log(String msg) {
    LOGGER.info(KafkaVaultProvider.class.getSimpleName() + " : " + msg);
  }

  /**
   * Builds the request URL for getting Vault secrets
   * 
   * @return URL to make GET request for data
   * @throws MalformedURLException
   */
  // Visible for unit testing
  protected static URL buildVaultRequestUrl(String requestURIBase, String apiVersion, String path)
      throws MalformedURLException {
    StringBuilder sb = new StringBuilder();
    String url = sb.append(requestURIBase).append("/").append(apiVersion).append("/").append(path).toString();
    return new URL(url);
  }

  /**
   * Sets the configurations for the Vault provider
   *
   * @param configs the set of new configuration
   * @return none
   */
  @Override
  public void configure(Map<String, ?> configs) {
    if (configs.containsKey(CONSTANT_REQUEST_URI_BASE)) {
      localProperties.setLocalProperty(CONSTANT_REQUEST_URI_BASE, (String) configs.get(CONSTANT_REQUEST_URI_BASE));
    } else {
      String requestURIBase = ENV_VAR_VAULT_ADDR.isEmpty() ? CONSTANT_LOCALHOST_URI : ENV_VAR_VAULT_ADDR;
      localProperties.setLocalProperty(CONSTANT_REQUEST_URI_BASE, requestURIBase);
    }

    if (X_VAULT_TOKEN != null)
      localProperties.setLocalProperty(CONSTANT_X_VAULT_TOKEN, X_VAULT_TOKEN);
    if (configs.containsKey(CONSTANT_X_VAULT_TOKEN)) {
      localProperties.setLocalProperty(CONSTANT_X_VAULT_TOKEN, (String) configs.get(CONSTANT_X_VAULT_TOKEN));
    }
  }

  @Override
  public void close() {
  }
}
