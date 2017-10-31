package com.rbaljinder.automation.karate.db;

import com.intuit.karate.ScriptContext;
import com.intuit.karate.ScriptValueMap;
import com.intuit.karate.StepDefs;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intuit.karate.Script.evalInNashorn;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class DatabaseStepDefs {

    private final StepDefs karateStepDefs;

    private final ScriptContext context;

    private Map<String, HikariDataSource> datasources = new HashMap<>();

    public DatabaseStepDefs(StepDefs karateStepDefs) {
        this.karateStepDefs = karateStepDefs;
        this.context = karateStepDefs.getContext();
    }

    @Given("^using database$")
    public void usingDatabase() {
        Map<String, Object> config = evalInNashorn("database", context).getAsMap();
        configureDatabase(config);
    }

    @Given("^using database (.*)$")
    public void usingDatabase(String databaseIdentifier) {
        Map<String, Object> config = evalInNashorn(databaseIdentifier, context).getAsMap();
        configureDatabase(config);
    }

    @Given("^database defined as$")
    public void usingDatabaseDefinedAs(List<KarateHikariConfig> configs) {
        KarateHikariConfig config = configs.get(0);
        configureDatabase(config.getDatabaseName(), config.getJdbcUrl(), config.getUsername(), config.getPassword(), config.getDriverClassName());
    }

    @Given("^define database with databaseName: (.+), url: (.+), username: (.+), password: (.+)$")
    public void configureDatabase(String databaseName, String url, String username, String password) {
        configureDatabase(databaseName, url, username, password, "oracle.jdbc.driver.OracleDriver");
    }

    void configureDatabase(Map<String, Object> config) {
        configureDatabase(config.get("databaseName").toString(),
                config.get("url").toString(),
                config.get("username").toString(),
                config.get("password").toString(),
                config.get("driverClassName").toString()
        );
    }

    @Given("^define database with databaseName: (.+), url: (.+), username: (.+), password: (.+), driverClassName: (.+)$")
    public void configureDatabase(String databaseName, String url, String username, String password, String driverClassName) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(StepExtensionUtils.resolve(url));
        hikariConfig.setDriverClassName(isBlank(StepExtensionUtils.resolve(driverClassName)) ? "oracle.jdbc.driver.OracleDriver" : driverClassName);
        hikariConfig.setUsername(StepExtensionUtils.resolve(username));
        hikariConfig.setPassword(StepExtensionUtils.resolve(password));

        if (isBlank(databaseName) || isBlank(url) || isBlank(username) || isBlank(password))
            throw new IllegalArgumentException("database configuration is incomplete, one of the following parameters is empty:" +
                    "databaseName[" + databaseName + "], username[" + username + "], password[" + password + "], url[" + url + "]");

        if (datasources.containsKey(databaseName))
            datasources.get(databaseName).close();
        datasources.put(databaseName, new HikariDataSource(hikariConfig));
    }

    @When("^query ([^\\s]+) =$")
    public void queryDoc(String result, String query) {
        if (datasources.isEmpty())
            throw new RuntimeException("attempt to query before defining a datasource, use \"define database with name: (.+), username: (.+), password: (.+)\"");
        if (datasources.size() > 1)
            throw new RuntimeException("there are multiple datasource configured, use \"query (.+) = (.+) on database: (.+)\" to query against a specific one");

        query(result, query, datasources.keySet().toArray(new String[0])[0]);
    }

    @When("^query database: (.+), (.+) = (.+)")
    public void query(String database, String name, String query) {
        QueryRunner queryRunner = new QueryRunner(datasources.get(database));
        try {
            List<Map<String, Object>> results = queryRunner.query(query, new MapListHandler());
            ScriptValueMap contextVars = context.getVars();
            if (results.size() == 0) {
                contextVars.put(name, null);
                return;
            }
            DocumentContext doc = JsonPath.parse(results.size() == 1 ? results.get(0) : results);
            contextVars.put(name, doc);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void afterScenario() {
        datasources.values().forEach(HikariDataSource::close);
    }

    public class KarateHikariConfig extends HikariConfig {
        String databaseName;

        public String getDatabaseName() {
            return databaseName;
        }

        public KarateHikariConfig setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }
    }
}


