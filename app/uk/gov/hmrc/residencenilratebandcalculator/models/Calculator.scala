/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.residencenilratebandcalculator.models

import javax.inject.{Inject, Singleton}

import org.joda.time.LocalDate
import play.api.Environment
import uk.gov.hmrc.residencenilratebandcalculator.converters.Percentify._

import scala.util.{Failure, Success, Try}

/* Terms of art:
 * Percentage closely inherited = The percentage of a property passing to a direct descendant
 */

@Singleton
class Calculator @Inject()(env: Environment) {

  lazy val residenceNilRateBand: GetNilRateAmountFromFile = {
    new GetNilRateAmountFromFile(env, "data/RNRB-amounts-by-year.json")
  }

  val taperRate = 2
  val earliestDisposalDate = new LocalDate(2015, 7, 8)
  val legislativeStartDate = new LocalDate(2017, 4, 6)


  def personsFormerAllowance(dateOfDisposal: LocalDate,
                             rnrbAtDisposal: Int,
                             broughtForwardAllowanceAtDisposal: Int,
                             adjustedBroughtForwardAllowance: Int): Int = {
    require(broughtForwardAllowanceAtDisposal >= 0, "broughtForwardAllowanceAtDisposal cannot be negative")
    require(adjustedBroughtForwardAllowance >= 0, "adjustedBroughtForwardAllowance cannot be negative")
    require(rnrbAtDisposal >= 0, "rnrbAtDisposal cannot be negative")

    val availableBroughtForwardAllowance = if (dateOfDisposal.isBefore(legislativeStartDate)) 0 else broughtForwardAllowanceAtDisposal
    val excessBroughtForwardAllowance = math.max(adjustedBroughtForwardAllowance - availableBroughtForwardAllowance, 0)

    rnrbAtDisposal + availableBroughtForwardAllowance + excessBroughtForwardAllowance
  }

  def adjustedBroughtForwardAllowance(totalAllowance: Int,
                                      amountToTaper: Int,
                                      broughtForwardAllowance: Int): Int = {
    require(totalAllowance >= 0, "totalAllowance cannot be negative")
    require(amountToTaper >= 0, "amountToTaper cannot be negative")
    require(broughtForwardAllowance >= 0, "broughtForwardAllowance cannot be negative")

    math.max(broughtForwardAllowance - (amountToTaper.toDouble * (broughtForwardAllowance.toDouble / totalAllowance)), 0.0) toInt
  }

  def lostRelievableAmount(valueOfDisposedProperty: Int,
                           formerAllowance: Int,
                           chargeablePropertyValue: Int,
                           taperedAllowance: Int): Int = {
    require(valueOfDisposedProperty >= 0, "valueOfDisposedProperty cannot be negative")
    require(formerAllowance > 0, "formerAllowance must be greater than zero")
    require(chargeablePropertyValue >= 0, "chargeablePropertyValue cannot be negative")
    require(taperedAllowance >= 0, "taperedAllowance cannot be negative")

    if (taperedAllowance == 0) {
      0
    }
    else {

      val percentageOfFormerAllowance = fractionAsBoundedPercent(valueOfDisposedProperty.toDouble / formerAllowance)
      val percentageOfAllowanceOnDeath = fractionAsBoundedPercent(chargeablePropertyValue.toDouble / taperedAllowance)
      val difference = boundedPercentageDifferenceAsDouble(percentageOfFormerAllowance,percentageOfAllowanceOnDeath)

      (difference * taperedAllowance.toDouble) toInt
    }
  }

  def downsizingAllowance(downsizingDetails: DownsizingDetails,
                          rnrbAtDisposal: Int,
                          totalAllowance: Int,
                          amountToTaper: Int,
                          broughtForwardAllowance: Int,
                          propertyValue: Int): Int = {

    val adjustedBroughtForward = adjustedBroughtForwardAllowance(totalAllowance, amountToTaper, broughtForwardAllowance)
    val formerAllowance = personsFormerAllowance(downsizingDetails.dateOfDisposal, rnrbAtDisposal, downsizingDetails.broughtForwardAllowanceAtDisposal, adjustedBroughtForward)
    val adjustedAllowance = taperedAllowance(totalAllowance, amountToTaper)
    val lostAmount = lostRelievableAmount(downsizingDetails.valueOfDisposedProperty, formerAllowance, propertyValue, adjustedAllowance)

    math.min(downsizingDetails.valueCloselyInherited, lostAmount)
  }

  def apply(input: CalculationInput): Try[CalculationResult] = {

    for {
      rnrbOnDeath <- residenceNilRateBand(input.dateOfDeath)
      rnrbAtDisposal <- input.downsizingDetails match {
        case Some(details) => residenceNilRateBand(details.dateOfDisposal)
        case None => Success(0)
      }
    } yield {
      val totalAllowance = rnrbOnDeath + input.broughtForwardAllowance
      val amountToTaper = math.max(input.grossEstateValue - TaperBand(input.dateOfDeath), 0) / taperRate
      val adjustedAllowance = taperedAllowance(totalAllowance, amountToTaper)

      val propertyCloselyInherited = input.propertyValueAfterExemption match {
        case Some(values) => values.valueCloselyInherited
        case None => (input.percentageCloselyInherited percent) * input.propertyValue toInt
      }

      val valueToConsider = propertyValueToConsider(input)

      val downsizingAddition: Int = input.downsizingDetails match {
        case Some(details) if details.dateOfDisposal isBefore earliestDisposalDate => 0
        case Some(details) => downsizingAllowance(details, rnrbAtDisposal, totalAllowance, amountToTaper, input.broughtForwardAllowance, valueToConsider)
        case None => 0
      }

      val residenceNilRateAmount = math.min(propertyCloselyInherited + downsizingAddition, adjustedAllowance)
      val carryForwardAmount = adjustedAllowance - residenceNilRateAmount

      CalculationResult(residenceNilRateAmount, rnrbOnDeath, carryForwardAmount)
    }
  }

  private def taperedAllowance(totalAllowance: Int, amountToTaper: Int) = math.max(totalAllowance - amountToTaper, 0)

  private def propertyValueToConsider(input: CalculationInput) = input.propertyValueAfterExemption match {
    case Some(values) => values.value
    case None => input.propertyValue
  }

  private def fractionAsBoundedPercent(v: Double) = math.min(v * 100, 100) percent

  private def boundedPercentageDifferenceAsDouble(a: Percent, b: Percent) = math.max((a - b) asDecimal, 0.0)
}
