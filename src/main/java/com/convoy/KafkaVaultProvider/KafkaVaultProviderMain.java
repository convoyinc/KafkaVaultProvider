package com.convoy.KafkaVaultProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.kafka.common.config.ConfigData;

public class KafkaVaultProviderMain {
  public static void main(String[] args) throws MalformedURLException, IOException {
    KafkaVaultProvider kvp = new KafkaVaultProvider();
    Map<String, String> configs = new HashMap<String, String>();
    kvp.configure(configs);
    String path = "production/kafka/your-service-prod";
    HashSet<String> keys = new HashSet<>(Arrays.asList("password"));

    ConfigData configData = kvp.get(path, keys);
    for (Map.Entry<String, String> entry : configData.data().entrySet()) {
      System.out.println("vault key: " + entry.getKey() + " ; value: " + entry.getValue());
    }

    kvp.close();
  }
}