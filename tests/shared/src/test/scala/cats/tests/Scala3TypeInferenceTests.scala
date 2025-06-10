/*
 * Copyright (c) 2015 Typelevel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cats.tests

import cats.*
import cats.data.*
import cats.syntax.all.*

class Scala3TypeInferenceTests extends CatsSuite {

  test("basic type inference improvements") {
    // These tests demonstrate improved type inference in Scala 3
    val opt = Option(42)
    val result = opt.map(_ * 2)
    assert(result === Some(84))
    
    val either: Either[String, Int] = Right(42)
    val mapped = either.map(_ * 2)
    assert(mapped === Right(84))
  }

  test("better traverse inference") {
    val list = List(1, 2, 3)
    val result = list.traverse(x => Option(x * 2))
    assert(result === Some(List(2, 4, 6)))
    
    // Should infer types better in Scala 3
    val nested = List(Some(1), Some(2), Some(3))
    val sequenced = nested.sequence
    assert(sequenced === Some(List(1, 2, 3)))
  }

  test("applicative operations with better inference") {
    val opt1 = Option(1)
    val opt2 = Option(2)
    val opt3 = Option(3)
    
    // mapN should have better inference
    val result = (opt1, opt2, opt3).mapN(_ + _ + _)
    assert(result === Some(6))
  }

  test("error handling improvements") {
    val either1: Either[String, Int] = Right(42)
    val either2: Either[String, Int] = Right(8)
    
    // Better inference for error handling
    val result = either1.flatMap(x => either2.map(y => x + y))
    assert(result === Right(50))
    
    val error: Either[String, Int] = Left("error")
    val handled = error.fold(identity, _.toString)
    assert(handled === "error")
  }

  test("chain operations with improved inference") {
    val chain = Chain(1, 2, 3)
    val doubled = chain.map(_ * 2)
    assert(doubled.toList === List(2, 4, 6))
    
    val traversed = chain.traverse(x => Option(x * 2))
    assert(traversed === Some(Chain(2, 4, 6)))
  }

  test("validated operations") {
    val valid = Validated.valid[String, Int](42)
    val invalid = Validated.invalid[String, Int]("error")
    
    // Better inference for Validated operations
    val result = valid.map(_ * 2)
    assert(result === Validated.valid(84))
    
    val combined = (valid, valid).mapN(_ + _)
    assert(combined === Validated.valid(84))
  }

  test("function composition") {
    val f: Int => Option[String] = x => Some(x.toString)
    val g: String => Option[Int] = s => s.toIntOption
    
    // Better inference for Kleisli composition
    val composed = Kleisli(f) andThen Kleisli(g)
    val result = composed.run(42)
    assert(result === Some(42))
  }

  test("parallel operations") {
    val opt1 = Option(1)
    val opt2 = Option(2)
    
    // Parallel operations should have better inference in Scala 3
    val result = (opt1, opt2).parMapN(_ + _)
    assert(result === Some(3))
  }

  test("nested data structures") {
    val nested: List[Option[Int]] = List(Some(1), Some(2), None, Some(3))
    val result = nested.sequence
    assert(result === None)
    
    val allSome: List[Option[Int]] = List(Some(1), Some(2), Some(3))
    val sequenced = allSome.sequence
    assert(sequenced === Some(List(1, 2, 3)))
  }

  test("show operations") {
    case class Person(name: String, age: Int)
    
    // In Scala 3, Show derivation should be automatic with derives clause
    implicit val showPerson: Show[Person] = Show.show(p => s"Person(${p.name}, ${p.age})")
    
    val person = Person("Alice", 30)
    assert(person.show === "Person(Alice, 30)")
  }

  test("type class summoning") {
    // Better summon syntax in Scala 3
    val functorOpt = summon[Functor[Option]]
    val result = functorOpt.map(Option(42))(_ * 2)
    assert(result === Some(84))
    
    val monadOpt = summon[Monad[Option]]
    val flatMapped = monadOpt.flatMap(Option(21))(x => Option(x * 2))
    assert(flatMapped === Some(42))
  }

  test("variance handling") {
    // Better variance inference
    val list: List[Any] = List(1, "hello", 3.14)
    val mapped = list.map(_.toString)
    assert(mapped === List("1", "hello", "3.14"))
    
    // Contravariant operations
    val showAny: Show[Any] = Show.fromToString
    assert(showAny.show(42) === "42")
  }

  test("type class composition") {
    // Better inference for composed type classes
    val optList: Option[List[Int]] = Some(List(1, 2, 3))
    val result = Functor[Option].compose[List].map(optList)(_ * 2)
    assert(result === Some(List(2, 4, 6)))
  }

  test("error recovery patterns") {
    val attempt: Either[Throwable, Int] = Try(42 / 0).toEither
    
    val recovered = attempt.recover {
      case _: ArithmeticException => 0
    }
    
    assert(recovered === Right(0))
  }

  test("resource management patterns") {
    // Simulated resource management with better inference
    def useResource[A](resource: String)(use: String => Either[String, A]): Either[String, A] = {
      try {
        use(resource)
      } catch {
        case e: Exception => Left(e.getMessage)
      }
    }
    
    val result = useResource("test")(r => Right(r.length))
    assert(result === Right(4))
  }
} 