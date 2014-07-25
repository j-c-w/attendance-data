/*
* Copyright 2013 Toshiyuki Takahashi
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

package main.backend.csv.scala.com.github.tototoshi.csv

import java.io._
import java.util.NoSuchElementException
import main.backend.csv.java.com.github.tototoshi.csv.LineReader

import scala.util.parsing.input.CharSequenceReader


class CSVReader protected (private val reader: Reader)(implicit format: CSVFormat) {

  val delimiter: Char = format.delimiter
  val quoteChar: Char = format.quoteChar

  private val parser = new CSVParser(format)

  private val lineReader = new LineReader(reader)

  def readNext(): Option[List[String]] = {

    @scala.annotation.tailrec
    def parseNext(lineReader: LineReader, leftOver: Option[String] = None, failureMsg: Option[String] = None): Option[List[String]] = {

      var nextLine = lineReader.readLineWithTerminator()

      if (nextLine == null) {
        if (leftOver.isDefined) {
          throw new MalformedCSVException(failureMsg.getOrElse(""))
        } else {
          None
        }
      } else {
        val line = leftOver.getOrElse("") + nextLine
        parser.parseLine(new CharSequenceReader(line, 0)) match {
          case parser.Success(result, _) => Some(result)
          case parser.Failure(msg, _) => {
            parseNext(lineReader, Some(line), Some(msg))
          }
          case parser.Error(msg, _) => throw new CSVParserException(msg)
        }
      }
    }

    parseNext(lineReader)
  }

  def foreach(f: Seq[String] => Unit): Unit = iterator.foreach(f)

  def iterator: Iterator[Seq[String]] = new Iterator[Seq[String]] {

    private var _next: Option[Seq[String]] = None

    def hasNext: Boolean = {
      _next match {
        case Some(row) => true
        case None => { _next = readNext;  _next.isDefined }
      }
    }

    def next(): Seq[String] = {
      _next match {
        case Some(row) => {
          val _row = row
          _next = None
          _row
        }
        case None => readNext.getOrElse(throw new NoSuchElementException("next on empty iterator"))
      }
    }

  }

  def toStream(): Stream[List[String]] =
    Stream.continually(readNext).takeWhile(_.isDefined).map(_.get)

  def all(): List[List[String]] = {
    toStream().toList
  }


  def allWithHeaders(): List[Map[String, String]] = {
    readNext() map { headers =>
      val lines = all()
      lines.map(l => headers.zip(l).toMap)
    } getOrElse List()
  }

  def close(): Unit = {
    lineReader.close()
    reader.close()
  }

}

object CSVReader {

  val DEFAULT_ENCODING = "UTF-8"

  def open(reader: Reader)(implicit format: CSVFormat): CSVReader =
    new CSVReader(reader)(format)

  def open(file: File)(implicit format: CSVFormat): CSVReader = {
    open(file, this.DEFAULT_ENCODING)(format)
  }

  def open(file: File, encoding: String)(implicit format: CSVFormat): CSVReader = {
    val fin = new FileInputStream(file)
    try {
      val reader = new InputStreamReader(fin, encoding)
      open(reader)(format)
    } catch {
      case e: UnsupportedEncodingException => fin.close(); throw e
    }
  }

  def open(filename: String)(implicit format: CSVFormat) : CSVReader =
    open(new File(filename), this.DEFAULT_ENCODING)(format)

  def open(filename: String, encoding: String)(implicit format: CSVFormat) : CSVReader =
    open(new File(filename), encoding)(format)

}