package com.convoy.KafkaVaultProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.config.ConfigException;

public class VaultApiCaller {

  public static String get(URL requestUrl, String xVaultToken) throws IOException {
    if (requestUrl.toString().isEmpty()) {
      KafkaVaultProvider.log("Expect vault request url to be non-empty, but empty string found");
      throw new ConfigException("Invalid request url");
    } else if (xVaultToken.isEmpty()) {
      KafkaVaultProvider.log("Expect xVaultToken to be non-empty, but empty string found");
      throw new ConfigException("Invalid xVaultToken");
    }

    // start connection
    HttpURLConnection openConnection = (HttpURLConnection) requestUrl.openConnection();
    openConnection.setRequestMethod("GET");
    openConnection.setRequestProperty("X-Vault-Token", xVaultToken);
    openConnection.connect();

    int responseCode = openConnection.getResponseCode();
    String responseMsg = openConnection.getResponseMessage();
    if (responseCode != 200) {
      throw new ConfigException(KafkaVaultProvider.class.getSimpleName()
          + ": Unsuccessful GET request for vault secrets, response code: " + responseCode);
    }

    String response = "";
    try {
      InputStream is = openConnection.getInputStream();
      response = IOUtils.toString(is, "utf-8");
      is.close();
    } catch (Exception e) {
      KafkaVaultProvider
          .log("response code: " + responseCode + " ; response msg: " + responseMsg + " ; ex msg: " + e.getMessage());
      throw e;
    }

    return response;
  }
}
