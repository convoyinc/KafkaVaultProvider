package com.convoy.KafkaVaultProvider;

import java.util.Properties;

public class ThreadLocalProperties extends Properties {
  /**
  *
  */
  private static final long serialVersionUID = 1L;
  private final ThreadLocal<Properties> localProperties = new ThreadLocal<Properties>() {
    @Override
    protected Properties initialValue() {
      return new Properties();
    }
  };

  public ThreadLocalProperties() {
    super();
    System.setProperty("javax.net.ssl.keyStore", "my-key-store");
  }

  public ThreadLocalProperties(Properties properties) {
    super();

    // Make the properties thread local from here. This to be done globally once.
    System.setProperties(new ThreadLocalProperties(System.getProperties()));

    // in each thread.
    System.setProperty("javax.net.ssl.keyStore", "my-key-store");
  }

  public String getLocalProperty(String key) {
    String localValue = localProperties.get().getProperty(key);
    return localValue == null ? super.getProperty(key) : localValue;
  }

  public Object setLocalProperty(String key, String value) {
    return localProperties.get().setProperty(key, value);
  }

}
