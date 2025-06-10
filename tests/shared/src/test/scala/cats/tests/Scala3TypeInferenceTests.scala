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
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cats.tests

import cats._
import cats.data._
import cats.syntax.all._

/**
 * Tests demonstrating functionality that benefits from Scala 3 type inference improvements.
 * These tests run on Scala 2.13+ but showcase patterns that would have better inference in Scala 3.
 */
class Scala3TypeInferenceTests extends CatsSuite {

  test("enhanced extension method type inference") {
    val list = List(1, 2, 3)
    val doubled = list.map(_ * 2)
    assert(doubled == List(2, 4, 6))
  }

  test("improved traverse with better type inference") {
    val list = List(1, 2, 3)
    val result = list.traverse(i => Option(i + 1))
    assert(result == Some(List(2, 3, 4)))

    // Test with nested structure - would benefit from Scala 3's improved inference
    val nested = List(List(1, 2), List(3, 4))
    val sequenced = nested.traverse(inner => inner.traverse(Option(_)))
    assert(sequenced == Some(List(List(1, 2), List(3, 4))))
  }

  test("either type for union-like behavior") {
    // Simulating union types with Either - Scala 3 would have native union types
    type StringOrInt = Either[String, Int]
    
    def processValue(value: StringOrInt): String = value match {
      case Left(s) => s
      case Right(i) => i.toString
    }
    
    assert(processValue(Left("hello")) == "hello")
    assert(processValue(Right(42)) == "42")
  }

  test("opaque type pattern") {
    // Test with a simple type alias pattern - Scala 3 would have true opaque types
    type UserId = String
    
    def createUser(id: UserId, name: String): (UserId, String) = (id, name)
    val user = createUser("user123", "John")
    assert(user._1 == "user123")
    assert(user._2 == "John")
  }

  test("enhanced applicative syntax") {
    val opt1 = Option(1)
    val opt2 = Option(2)
    
    val result = (opt1, opt2).mapN(_ + _)
    assert(result == Some(3))
  }

  test("improved functor composition") {
    val listOpt: List[Option[Int]] = List(Some(1), Some(2), None, Some(4))
    val result = listOpt.map(_.map(_ * 2))
    assert(result == List(Some(2), Some(4), None, Some(8)))
  }

  test("enhanced validation") {
    val valid = Validated.valid[String, Int](42)
    val result = valid.map(_ * 2)
    assert(result == Validated.valid(84))
  }

  test("better monad comprehensions") {
    val result = for {
      x <- Option(1)
      y <- Option(2)
      z <- Option(3)
    } yield x + y + z
    
    assert(result == Some(6))
  }

  test("improved error handling with Either") {
    val result: Either[String, Int] = Right(42)
    val mapped = result.map(_ * 2)
    assert(mapped == Right(84))
  }

  test("context function patterns") {
    // Test type class resolution - would benefit from Scala 3's improved inference
    def mapWithFunctor[F[_]: Functor, A, B](fa: F[A])(f: A => B): F[B] = 
      Functor[F].map(fa)(f)
    
    val result = mapWithFunctor(Option(42))(_ * 2)
    assert(result == Some(84))
  }

  test("enhanced chain operations") {
    val chain = Chain(1, 2, 3, 4, 5)
    val result = chain.map(_ * 2).filter(_ > 4)
    assert(result.toList == List(6, 8, 10))
  }

  test("improved kleisli composition") {
    val k1 = Kleisli[Option, Int, String](i => Some(i.toString))
    val k2 = Kleisli[Option, String, Int](s => s.toIntOption)
    val composed = k1 andThen k2
    
    val result = composed.run(42)
    assert(result == Some(42))
  }

  test("better error recovery") {
    def divide(x: Int, y: Int): Either[String, Int] = 
      if (y == 0) Left("Division by zero") else Right(x / y)
    
    val attempt: Either[String, Int] = divide(42, 0)
    val recovered = attempt.fold(_ => Right(0), Right(_))
    assert(recovered == Right(0))
  }

  test("enhanced sequence operations") {
    val options = List(Option(1), Option(2), Option(3))
    val mappedOptions = options.map(_.map(_ * 2))
    assert(mappedOptions == List(Some(2), Some(4), Some(6)))
    
    val withNone = List(Some(1), None, Some(3))
    val filteredNone = withNone.collect { case Some(x) => x }
    assert(filteredNone == List(1, 3))
  }

  test("improved parallel-like operations") {
    // Using simple applicative operations that work well
    val opt1 = Option(List(1, 2, 3))
    val opt2 = Option(List(4, 5, 6))
    
    val combined = (opt1, opt2).mapN(_ ++ _)
    assert(combined == Some(List(1, 2, 3, 4, 5, 6)))
  }

  test("type class instance summoning") {
    // Demonstrating patterns that would benefit from Scala 3's improved inference
    val functorOpt = Functor[Option]
    val result = functorOpt.map(Option(42))(_ * 2)
    assert(result == Some(84))
    
    val monadOpt = Monad[Option]
    val flatMapped = monadOpt.flatMap(Option(21))(x => Option(x * 2))
    assert(flatMapped == Some(42))
  }
} 