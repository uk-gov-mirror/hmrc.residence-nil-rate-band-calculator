Feature: Property Exemptions

  Additional scenarios covering cases where part of the property is exempt from Inheritance Tax

  Scenario: 1 - Property is partially exempt, and the remaining part is lower than the applicable nil-rate band
    When I combine these details
      | dateOfDeath                | 2021-01-01 |
      | chargeableTransferAmount   | 490000     |
      | grossEstateValue           | 490000     |
      | propertyValue              | 300000     |
      | percentageCloselyInherited | 100        |
      | broughtForwardAllowance    | 0          |
    And these property value after exemption details
      | value                 | 100000 |
      | valueCloselyInherited | 100000 |
    And POST the details to calculate
    Then I should get an OK response
    And the response body should be
      | applicableNilRateBandAmount | 175000 |
      | residenceNilRateAmount      | 100000 |
      | carryForwardAmount          | 75000  |