Feature: Case Study 13

  Variants on Case Study 13, a simple example of downsizing

  Scenario: 13.1 - Case Study 13
    When I combine these details
      | dateOfDeath                | 2020-09-01 |
      | chargeableTransferAmount   | 305000     |
      | grossEstateValue           | 305000     |
      | propertyValue              | 105000     |
      | percentageCloselyInherited | 100        |
      | broughtForwardAllowance    | 0          |
    And these downsizing details
      | dateOfDisposal                    | 2018-05-01 |
      | valueOfDisposedProperty           | 500000     |
      | valueCloselyInherited             | 200000     |
      | broughtForwardAllowanceAtDisposal | 0          |
    And POST the details to calculate
    Then I should get an OK response
    And the response body should be
      | applicableNilRateBandAmount | 175000 |
      | residenceNilRateAmount      | 175000 |
      | carryForwardAmount          | 0      |