Feature: database query

  Scenario: should query database
    * using database
    When query flowCode =
    """
      select * from flow_code where id = 23
    """

    Then match flowCode.code == 'ABC'

  Scenario: should query database
    * using database database.primary
    When query flowCode =
    """
      select * from flow_code where id = 23
    """

    Then match flowCode.code == 'ABC'
    And query allFlows =
    """
      select * from flow_code
    """
    And match allFlows == '#[2]'

  Scenario: should query database and excessive matching
    * using database
    Given def flowCodeId = 23
    When query flowCodes =
    """
      select * from flow_code
    """

    Then match flowCodes == '#[2]'
    And eval def abcFlowCode = get flowCodes $[?(@.id == #(flowCodeId))]
    And print abcFlowCode
    And match abcFlowCode[0].code == 'ABC'

  Scenario: should query database defined in scenario
    * database defined as
      | databaseName | driverClassName       | username | password                               | jdbcUrl                            |
      | dbName       | org.postgresql.Driver | external | _RESOLVE_ENV_database.primary.password | jdbc:postgresql://host:5432/dbName |
      | dbName       | org.postgresql.Driver | external | _RESOLVE_ENV_database.primary.password | jdbc:postgresql://host:5432/dbName |

    Given def flowCodeId = 23
    When query flowCodes =
    """
      select * from flow_code
    """

    Then match flowCodes == '#[2]'
    And eval def abcFlowCode = get flowCodes $[?(@.id == #(flowCodeId))]
    And print abcFlowCode
    And match abcFlowCode[0].code == 'ABC'