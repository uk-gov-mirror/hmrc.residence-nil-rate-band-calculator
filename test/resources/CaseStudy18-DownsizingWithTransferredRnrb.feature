@CaseStudy18
@CaseStudy
@Downsizing
Feature: Case Study 18

  Variants on Case Study 18, downsizing from a property which was worth less than the available RNRB

  Scenario: 18.1 - Case Study 18
    When I combine these details
      | dateOfDeath                | 2021-03-01 |
      | chargeableEstateValue   | 500000     |
      | valueOfEstate              | 500000     |
      | propertyValue              | 0          |
      | percentagePassedToDirectDescendants | 0          |
      | valueBeingTransferred    | 175000     |
    And these downsizing details
      | datePropertyWasChanged                    | 2018-10-01 |
      | valueOfChangedProperty           | 285000     |
      | valueOfAssetsPassing             | 250000     |
      | valueAvailableWhenPropertyChanged | 125000     |
    And POST the details to calculate
    Then I should get an OK response
    And the response body should be
      | applicableNilRateBandAmount | 175000 |
      | residenceNilRateAmount      | 250000 |
      | carryForwardAmount          | 100000 |
      | defaultAllowanceAmount      | 350000 |
      | adjustedAllowanceAmount     | 350000 |
