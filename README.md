# KafkaVaultProvider

An extension to the Kafka Connect ConfigProvider to support accessing Vault secrets via Hashicorp Vault API call.

[![CircleCI](https://circleci.com/gh/convoyinc/KafkaVaultProvider.svg?style=svg)](https://circleci.com/gh/convoyinc/KafkaVaultProvider)

### Implements the interface ConfigProvider (org.apache.kafka.common.config.provider)

https://kafka.apache.org/21/javadoc/org/apache/kafka/common/config/provider/ConfigProvider.html

| Type       | Method                            | Description                                                            |
| ---------- | --------------------------------- | ---------------------------------------------------------------------- |
| ConfigData | get(String key)                   | Retrieves data from Vault path that matches a given key                |
| ConfigData | get(String url, Set<String> keys) | Retrieves values from Vault url with a given set of keys               |
| void       | configure(Map<String, ?> configs) | Sets internal configuration values needed to set up a vault connection |

---

### To use in Kafka Connect:

1. Clone project and build Jar file.

- To test the main function in a local dev env, put these jar files into `src/main/lib`:
- connect-2.1.0.jar https://mvnrepository.com/artifact/org.apache.kafka/connect-api/2.1.0
- json-20190722.jar https://mvnrepository.com/artifact/org.json/json/20190722
- kafka-clients-2.1.0.jar https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients/2.1.0

2. Put your final Jar file, say KafkaVaultProvider.jar, under the Kafka worker plugin folder. Default is /usr/share/java. PLUGIN_PATH in the Kafka worker config file.

3. Upload all the dependency jars to PLUGIN_PATH as well. Use the META-INFO/MANIFEST.MF file inside your Jar file to configure the 'ClassPath' of dependent jars that your code will use.

4. In kafka worker config file, create two additional properties:

```
CONNECT_CONFIG_PROVIDERS: 'vault', // Alias name of your ConfigProvider
CONNECT_CONFIG_PROVIDERS_VAULT_CLASS:'com.convoy.KafkaVaultProvider.KafkaVaultProvider',
```

5. Restart workers

6. Update your connector config file by curling POST to Kafka Restful API. In Connector config file, you could reference the value inside `ConfigData` returned from `ConfigProvider:get(path, keys)` by using the syntax like:

```
database.password=${mycustom:/path/pass/to/get/method:password}
```

`ConfigData` is a HashMap which contains {password: 123}

7. If you still seeing ClassNotFound exception, probably your ClassPath is not setup correctly.

Note:

- If you are using AWS ECS/EC2, you need to set the worker config file by setting the environment variable.
- The worker config and connector config files are different.

---

### Example usage:

- Clone and build KafkaVaultProvider in docker-build terraform

```
# Clone and build KafkaVaultProvider
rm -rf KafkaVaultProvider
git clone https://github.com/convoyinc/KafkaVaultProvider.git
cd KafkaVaultProvider
mvn clean install -DskipTests
cd ..
```

- Add JAR file in terraform Dockerfile

```
# Copy KafkaVaultProvider to plugin path
RUN mkdir -p ${CONNECT_PLUGIN_PATH}/KafkaVaultProvider/classes
ADD ./KafkaVaultProvider/target/KafkaVaultProvider.jar ${CONNECT_PLUGIN_PATH}/KafkaVaultProvider/KafkaVaultProvider.jar
ADD ./KafkaVaultProvider/target/classes/json-2019.jar ${CONNECT_PLUGIN_PATH}/KafkaVaultProvider/classes/json-2019.jar
```

- Update deployment terraform container environment definition

```javascript
{
  name: "CONNECT_CONFIG_PROVIDERS",
  value: 'file,vault'
},
{
  name: "CONNECT_CONFIG_PROVIDERS_FILE_CLASS",
  value: 'org.apache.kafka.common.config.provider.FileConfigProvider'
},
{
  name: "CONNECT_CONFIG_PROVIDERS_VAULT_CLASS",
  value: 'com.convoy.KafkaVaultProvider.KafkaVaultProvider'
}
```

- Redeploy Kafka connect

---

Example Java usage:

1. Configure

```Java
Map<String,String> configs = new HashMap<String,String>();
configs.put("request_uri_base", "http://127.0.0.1:8200/v1/");
configs.put("secret_type", "secret");
configs.put("secret_directory", "dummy-secrets");
configs.put("x_vault_token", "token-here");
kvp.configure(configs);
```

2.  Retrieve Value

```Java
 kvp.get("secretPassword").data().get("secretKey")
```

---

Helpful info:

- Use this library like this: https://docs.confluent.io/current/connect/security.html#externalizing-secrets

- Retrieving values in the Kafka Connect worker like this: https://rmoff.net/2019/05/24/putting-kafka-connect-passwords-in-a-separate-file-/-externalising-secrets/

- More info on Vault API calls: https://learn.hashicorp.com/vault/getting-started/apis

---

Special Thanks to: **@adriank-convoy @Samlinxia**
