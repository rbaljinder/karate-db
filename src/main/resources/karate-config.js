function config () {
  var env = karate.env // get java system property 'karate.env'
  karate.log('karate.env system property was:', env)

  var config = {
    database: {
      databaseName: 'patientflow',
      driverClassName: 'org.postgresql.Driver',
      username: null,
      password: '_RESOLVE_ENV_database.primary.password',
      url: 'jdbc:postgresql://35.185.118.131:5432/patientflow',

      primary: {
        databaseName: 'patientflow',
        driverClassName: 'org.postgresql.Driver',
        username: null,
        password: '_RESOLVE_ENV_database.primary.password',
        url: 'jdbc:postgresql://35.185.118.131:5432/patientflow'
      }
    }
  }

  if (env === 'psuat') {
    config.database.username = 'external'
    config.database.primary.username = 'external'
  }

  if (env === 'prod') {
    config.database.username = 'external'
    config.database.primary.username = 'external'
  }

  karate.configure('connectTimeout', 50000)
  karate.configure('readTimeout', 50000)
  return config
}