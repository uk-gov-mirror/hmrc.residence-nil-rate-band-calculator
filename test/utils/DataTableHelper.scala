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

package utils

import cucumber.api.DataTable
import play.api.libs.json.{JsObject, Json}
import scala.collection.JavaConverters._

object DataTableHelper {

  def convertToJsonString(dataTable: DataTable) = {
    convertToJsValue(dataTable).toString
  }

  def convertToJsonNode(key: String, value: String) = key match {
    case "dateOfDeath" => (key, Json.toJson(value))
    case "dateOfDisposal" => (key, Json.toJson(value))
    case _ => (key, Json.toJson(value.toInt))
  }

  def convertToJsValue(dataTable: DataTable) ={
    val nodes = dataTable.asMap(classOf[String], classOf[String]).asScala

    val convertedNodes = nodes map {
      case(k, v) => convertToJsonNode(k, v)
    }

    JsObject(convertedNodes)
  }
}
