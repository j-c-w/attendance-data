package main.scala.datatypes.builders

import main.scala.datatypes.EmployeeTypes
import main.scala.datatypes.options._

/*
 * Created by Jackson Woodruff on 23/07/2014 
 *
 *
 * Designed to make it easy to leave data out
 * when it is not needed (i.e. after an aggregate,
 * where a lot of data is not relevant)
 */

class EmployeeTypeBuilder {
  self =>

  var employees: DoubleOption = NoDouble
  var present: DoubleOption = NoDouble
  var percentAbsent: DoubleOption = NoDouble
  var absent: DoubleOption = NoDouble
  var leave: DoubleOption = NoDouble

  def build: EmployeeTypes = new EmployeeTypes(
    employees,
    present,
    absent,
    percentAbsent,
    leave
  )

  //AVERAGES the two data points
  def mergeAverage(other: EmployeeTypeBuilder) = new EmployeeTypeBuilder {
    employees = (self.employees + other.employees) / SomeDouble(2)
    present = (self.present + other.present) / SomeDouble(2)
    absent = (self.absent + other.absent) / SomeDouble(2)
    leave = (self.leave + other.leave) / SomeDouble(2)
    //leaves out percent for the moment
  }

  //SUMS the data points
  def mergeSum(other: EmployeeTypeBuilder) = new EmployeeTypeBuilder {
    employees = self.employees + other.employees
    present = self.present + other.present
    absent = self.absent + other.absent
    leave = self.absent + other.absent
  }
}

