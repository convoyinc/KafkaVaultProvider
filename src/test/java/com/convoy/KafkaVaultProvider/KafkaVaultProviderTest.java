package com.convoy.KafkaVaultProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

class KafkaVaultProviderTest {

  String TEST_URI_BASE = "http://127.0.0.1:8200";

  Map<String, String> setupConfig() {
    Map<String, String> configs = new HashMap<String, String>();
    configs.put(KafkaVaultProvider.CONSTANT_REQUEST_URI_BASE, TEST_URI_BASE);
    configs.put(KafkaVaultProvider.CONSTANT_X_VAULT_TOKEN, "token-here");
    return configs;
  }

  @Test
  void testConfigureWithFullValidConfigMap() {
    KafkaVaultProvider kvp = new KafkaVaultProvider();
    Map<String, String> configs = setupConfig();
    kvp.configure(configs);
    assertEquals("token-here", kvp.localProperties.getLocalProperty(KafkaVaultProvider.CONSTANT_X_VAULT_TOKEN));
    kvp.close();
  }

  @Test
  void testBulidVaultRequestUriWithValidConfig() throws MalformedURLException {
    KafkaVaultProvider kvp = new KafkaVaultProvider();
    Map<String, String> configs = setupConfig();
    kvp.configure(configs);

    URL validURL = KafkaVaultProvider.buildVaultRequestUrl(TEST_URI_BASE, "v1", "testDirectory");
    assertEquals("http://127.0.0.1:8200/v1/testDirectory", validURL.toString());
    kvp.close();
  }

  @Test
  void testBulidVaultRequestUriWithBadConfig() throws MalformedURLException {
    KafkaVaultProvider kvp = new KafkaVaultProvider();
    Map<String, String> configs = setupConfig();
    configs.put(KafkaVaultProvider.CONSTANT_REQUEST_URI_BASE, "");
    String path = "secret/data/dummy-secrets";
    configs.put(KafkaVaultProvider.CONSTANT_SECRET_DIRECTORY, path);
    kvp.configure(configs);
    assertThrows(MalformedURLException.class, () -> {
      KafkaVaultProvider.buildVaultRequestUrl("", "v1", path);
    });
    kvp.close();
  }

  @Test
  void testGetStringWithBadConfig() throws ConfigException, MalformedURLException {
    KafkaVaultProvider kvp = new KafkaVaultProvider();
    // Invalid empty secrety_directory
    assertThrows(ConfigException.class, () -> {
      kvp.get("");
    });

    Map<String, String> configs = setupConfig();
    // MalformedURLException due to base URI being empty.
    configs.put(KafkaVaultProvider.CONSTANT_REQUEST_URI_BASE, "");
    kvp.configure(configs);
    assertThrows(MalformedURLException.class, () -> {
      kvp.getVaultData("data");
    });
    kvp.close();
  }

  @Test
  void testClose() {
    KafkaVaultProvider kvp = new KafkaVaultProvider();
    kvp.close();
    assertTrue(true);
  }

}
