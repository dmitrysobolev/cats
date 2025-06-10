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

package cats.syntax

import cats.*
import cats.data.*

/**
 * Scala 3 specific syntax improvements that leverage enhanced type inference
 */
trait Scala3TypeInferenceSyntax {

  // Enhanced traverse syntax with better type inference
  extension [F[_], A](fa: F[A])
    /** Traverse with polymorphic function syntax - better type inference */
    def traverseP[G[_]: Applicative, B](f: [X] => X => G[X])(using Traverse[F]): G[F[A]] =
      fa.traverse(a => f(a))

    /** ParTraverse with better inference */
    def parTraverseInferred[G[_], B](f: A => G[B])(using 
        Traverse[F], 
        Parallel[G]
    ): G[F[B]] = 
      Parallel.parTraverse(fa)(f)

  // Better functor composition inference
  extension [F[_]: Functor, G[_]: Functor, A](ffga: F[F[G[A]]])
    /** Flatten nested functors with better inference */
    def flattenFunctor: F[G[A]] = ffga.map(identity)

  // Enhanced monad syntax with context function types
  extension [F[_], A](fa: F[A])
    /** FlatMap with context functions for better inference */
    def flatMapCtx[B](f: A ?=> F[B])(using Monad[F]): F[B] =
      fa.flatMap(a => f(using a))

  // Better applicative syntax
  extension [F[_]: Applicative, A](fa: F[A])
    /** Apply with tuple syntax and better inference */
    def applyTuple[B, C](fb: F[B])(f: (A, B) => C): F[C] =
      (fa, fb).mapN(f)
    
    /** Multiple apply with better inference */
    def applyAll[B, C, D](fb: F[B], fc: F[C])(f: (A, B, C) => D): F[D] =
      (fa, fb, fc).mapN(f)

  // Enhanced Kleisli composition with better inference
  extension [F[_]: Monad, A, B](f: A => F[B])
    /** Compose Kleisli arrows with better type inference */
    def >=>[C](g: B => F[C]): A => F[C] = 
      Kleisli(f) >=> Kleisli(g)

  // Union type syntax for better error handling
  extension [A](a: A)
    /** Convert to Either with union type support */
    def asRight[E]: Either[E, A] = Right(a)
    def asLeft[B]: Either[A, B] = Left(a)

  // Better type inference for Validated
  extension [A](a: A)
    /** Validated constructors with better inference */
    def validNel[E]: ValidatedNel[E, A] = Validated.validNel(a)
    def invalidNel[B]: ValidatedNel[A, B] = Validated.invalidNel(a)

  // Enhanced Chain operations with better inference
  extension [A](as: Chain[A])
    /** Traverse Chain with better inference */
    def traverseChain[F[_]: Applicative, B](f: A => F[B]): F[Chain[B]] =
      as.traverse(f)

  // Better inference for type class derivation helpers
  object InferenceHelpers:
    /** Summon with better error messages */
    inline def summon[T](using t: T): T = t
    
    /** Get type class instance with better inference */
    inline def instance[F[_], TC[_[_]]](using tc: TC[F]): TC[F] = tc
    
    /** Derive instances with better inference */
    inline def derive[F[_], TC[_[_]]](using mirror: deriving.Mirror.Of[F], tc: TC[F]): TC[F] = tc

  // Extension methods for better HKT inference
  extension [F[_]](F: Functor[F])
    /** Lift function with better inference */
    def liftF[A, B](f: A => B): F[A] => F[B] = F.map(_)(f)

  extension [F[_]](F: Applicative[F])
    /** Lift function2 with better inference */
    def liftA2[A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] = 
      F.map2(_, _)(f)

  // Better inference for Option and Either operations
  extension [A](opt: Option[A])
    /** Convert Option to Either with better inference */
    def toEither[E](error: => E): Either[E, A] = opt.toRight(error)
    
    /** Convert to Validated with better inference */
    def toValidated[E](error: => E): Validated[E, A] = opt.toValidNel(error)

  extension [E, A](either: Either[E, A])
    /** Convert Either to Validated with better inference */
    def toValidated: Validated[E, A] = either.toValidated

  // Enhanced syntax for nested data structures
  extension [F[_]: Traverse, G[_]: Applicative, A](fga: F[G[A]])
    /** Sequence with better inference */
    def sequenceInferred: G[F[A]] = fga.sequence

  // Better inference for resource management
  extension [F[_]: Monad, A](fa: F[A])
    /** Bracket with better inference */
    def bracketInferred[B, C](
        use: A => F[B]
    )(release: A => F[C])(using MonadError[F, Throwable]): F[B] =
      fa.flatMap(a => 
        use(a).attempt.flatMap {
          case Right(b) => release(a).as(b)
          case Left(e) => release(a) *> MonadError[F, Throwable].raiseError(e)
        }
      )
}

object scala3TypeInference extends Scala3TypeInferenceSyntax 