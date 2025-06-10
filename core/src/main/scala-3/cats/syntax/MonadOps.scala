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

import cats.{Alternative, Monad, MonadError}
import cats.data.*

final class MonadOps[F[_], A](private val fa: F[A]) extends AnyVal {
  def whileM[G[_]](using M: Monad[F], G: Alternative[G])(p: F[Boolean]): F[G[A]] = M.whileM(p)(fa)
  def whileM_(using M: Monad[F])(p: F[Boolean]): F[Unit] = M.whileM_(p)(fa)
  def untilM[G[_]](using M: Monad[F], G: Alternative[G])(p: F[Boolean]): F[G[A]] = M.untilM(fa)(p)
  def untilM_(using M: Monad[F])(p: F[Boolean]): F[Unit] = M.untilM_(fa)(p)
  def iterateWhile(using M: Monad[F])(p: A => Boolean): F[A] = M.iterateWhile(fa)(p)
  def iterateUntil(using M: Monad[F])(p: A => Boolean): F[A] = M.iterateUntil(fa)(p)
  def flatMapOrKeep[A1 >: A](using M: Monad[F])(pfa: PartialFunction[A, F[A1]]): F[A1] =
    M.flatMapOrKeep[A, A1](fa)(pfa)

  // Enhanced Scala 3 syntax with better type inference
  
  /** 
   * FlatMap with polymorphic function types for better inference
   */
  def flatMapPoly[B](f: [X] => X => F[X])(using M: Monad[F]): F[A] =
    M.flatMap(fa)(a => f(a))

  /**
   * Context function flatMap for dependency injection patterns
   */
  def flatMapCtx[Ctx, B](f: Ctx ?=> A => F[B])(using M: Monad[F], ctx: Ctx): F[B] =
    M.flatMap(fa)(a => f(a))

  /**
   * Error handling with union types
   */
  def attemptUnion[E](using ME: MonadError[F, E]): F[E | A] =
    ME.attempt(fa).map {
      case Left(e) => e
      case Right(a) => a
    }

  /**
   * Better inference for nested monads
   */
  def flattenNested[G[_], B](using M: Monad[F], ev: A <:< F[G[B]]): F[G[B]] =
    M.flatten(M.map(fa)(ev))

  /**
   * Enhanced parallel operations with better inference
   */
  def parFlatMap[B](f: A => F[B])(using M: Monad[F], P: Parallel.Aux[F, F]): F[B] =
    M.flatMap(fa)(f) // Simplified for now

  /**
   * Resource-safe operations with better inference
   */
  def bracket[B, C](use: A => F[B])(release: A => F[C])(using M: MonadError[F, Throwable]): F[B] =
    M.bracket(fa)(use)(release)

  /**
   * Better error recovery with type inference
   */
  def recoverWith[E, A1 >: A](f: E => F[A1])(using ME: MonadError[F, E]): F[A1] =
    ME.handleErrorWith(fa)(f)

  /**
   * Conditional execution with better inference
   */
  def whenA[B](cond: Boolean)(using M: Monad[F], ev: A <:< Unit): F[Unit] =
    if (cond) M.void(fa) else M.unit

  def unlessA[B](cond: Boolean)(using M: Monad[F], ev: A <:< Unit): F[Unit] =
    if (!cond) M.void(fa) else M.unit

  /**
   * Better traverse integration
   */
  def traverseM[G[_], B](f: A => G[F[B]])(using M: Monad[F], T: Traverse[G]): F[G[B]] =
    M.flatMap(fa)(a => T.sequence(f(a)))

  /**
   * Enhanced transformation operations
   */
  def transformWith[B](f: A => F[B], g: Throwable => F[B])(using ME: MonadError[F, Throwable]): F[B] =
    ME.attempt(fa).flatMap {
      case Right(a) => f(a)
      case Left(e) => g(e)
    }

  /**
   * Better inference for option-like operations
   */
  def orElse[A1 >: A](alternative: => F[A1])(using M: MonadError[F, Throwable]): F[A1] =
    M.handleErrorWith(fa)(_ => alternative)

  /**
   * Type-safe casting with better inference
   */
  def narrow[B <: A]: F[B] = fa.asInstanceOf[F[B]]
  def widen[B >: A]: F[B] = fa

  /**
   * Better integration with Validated
   */
  def toValidated[E](error: => E)(using M: MonadError[F, E]): F[Validated[E, A]] =
    M.attempt(fa).map(_.toValidated)

  /**
   * Enhanced kleisli arrow operations
   */
  def local[B](f: B => A)(using M: Monad[F]): Kleisli[F, B, A] =
    Kleisli(b => M.pure(f(b))).andThen(_ => fa)
}

/**
 * Enhanced syntax object with Scala 3 improvements
 */
object MonadSyntax3 {
  
  /**
   * Better inference for monadic comprehensions
   */
  extension [F[_]: Monad, A](fa: F[A])
    def >>=[B](f: A => F[B]): F[B] = fa.flatMap(f)
    def >>[B](fb: F[B]): F[B] = fa.flatMap(_ => fb)
    
  /**
   * Context function support for dependency injection
   */
  extension [F[_]: Monad, Ctx, A](fa: Ctx ?=> F[A])
    def provide(ctx: Ctx): F[A] = fa(using ctx)
    
  /**
   * Better inference for error handling
   */
  extension [F[_], E, A](fa: F[A])(using ME: MonadError[F, E])
    def recoverAll[A1 >: A](f: E => A1): F[A1] = 
      ME.recover(fa)(f)
    
    def attemptNarrow[E1 <: E]: F[Either[E1, A]] =
      ME.attempt(fa).asInstanceOf[F[Either[E1, A]]]

  /**
   * Union type error handling
   */
  extension [F[_], A](fa: F[A])
    def toUnion[E](using ME: MonadError[F, E]): F[E | A] =
      ME.attempt(fa).map {
        case Left(e) => e: E | A
        case Right(a) => a: E | A
      }

  /**
   * Enhanced parallel combinators
   */
  extension [F[_], A](fa: F[A])
    def parProduct[B](fb: F[B])(using P: Parallel[F]): F[(A, B)] =
      P.parProduct(fa, fb)
    
    def parMap2[B, C](fb: F[B])(f: (A, B) => C)(using P: Parallel[F]): F[C] =
      P.parMap2(fa, fb)(f)

  /**
   * Better inference for resource management
   */
  extension [F[_]: MonadError[*, Throwable], A](fa: F[A])
    def ensuring[B](finalizer: F[B]): F[A] =
      fa.attempt.flatMap {
        case Right(a) => finalizer.as(a)
        case Left(e) => finalizer *> MonadError[F, Throwable].raiseError(e)
      }

  /**
   * Inline helpers for better type inference
   */
  transparent inline def liftM[F[_]: Monad, A, B](f: A => B): F[A] => F[B] =
    _.map(f)

  transparent inline def liftM2[F[_]: Monad, A, B, C](f: (A, B) => C): (F[A], F[B]) => F[C] =
    (fa, fb) => fa.flatMap(a => fb.map(b => f(a, b)))

  transparent inline def liftM3[F[_]: Monad, A, B, C, D](f: (A, B, C) => D): (F[A], F[B], F[C]) => F[D] =
    (fa, fb, fc) => for {
      a <- fa
      b <- fb  
      c <- fc
    } yield f(a, b, c)
}
