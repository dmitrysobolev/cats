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

package cats

import cats.data.*
import scala.util.{Try, Success, Failure}

/**
 * Scala 3 specific given instances that improve type inference
 * through better implicit resolution and context function types
 */
object GivenInstances:

  // Better type inference for common type class instances
  given optionFunctor: Functor[Option] = cats.instances.option.catsStdInstancesForOption
  given listFunctor: Functor[List] = cats.instances.list.catsStdInstancesForList
  given vectorFunctor: Functor[Vector] = cats.instances.vector.catsStdInstancesForVector
  given tryFunctor: Functor[Try] = cats.instances.try_.catsStdInstancesForTry

  given optionMonad: Monad[Option] = cats.instances.option.catsStdInstancesForOption
  given listMonad: Monad[List] = cats.instances.list.catsStdInstancesForList
  given vectorMonad: Monad[Vector] = cats.instances.vector.catsStdInstancesForVector
  given tryMonad: Monad[Try] = cats.instances.try_.catsStdInstancesForTry

  // Enhanced Either instances with better inference
  given [E]: Functor[Either[E, *]] = cats.instances.either.catsStdInstancesForEither[E]
  given [E]: Monad[Either[E, *]] = cats.instances.either.catsStdInstancesForEither[E]
  given [E]: MonadError[Either[E, *], E] = cats.instances.either.catsStdInstancesForEither[E]

  // Validated instances with better inference
  given [E: Semigroup]: Applicative[Validated[E, *]] = 
    cats.data.Validated.catsDataApplicativeErrorForValidated[E]
  given [E: Semigroup]: ApplicativeError[Validated[E, *], E] = 
    cats.data.Validated.catsDataApplicativeErrorForValidated[E]

  // Chain instances with optimized inference
  given chainMonad: Monad[Chain] = cats.data.Chain.catsDataInstancesForChain
  given chainTraverse: Traverse[Chain] = cats.data.Chain.catsDataInstancesForChain
  given chainAlternative: Alternative[Chain] = cats.data.Chain.catsDataInstancesForChain

  // NonEmptyList instances
  given nelSemigroup[A]: Semigroup[NonEmptyList[A]] = cats.data.NonEmptyList.catsDataSemigroupForNonEmptyList
  given nelFunctor: Functor[NonEmptyList] = cats.data.NonEmptyList.catsDataInstancesForNonEmptyList
  given nelMonad: Monad[NonEmptyList] = cats.data.NonEmptyList.catsDataInstancesForNonEmptyList

  // Function composition instances with better inference
  given [R]: Monad[Function1[R, *]] = cats.instances.function.catsStdInstancesForFunction1[R]
  given [R]: Functor[Function1[R, *]] = cats.instances.function.catsStdInstancesForFunction1[R]

  // Context function helpers for better inference
  type ContextFunction[A, B, F[_]] = A ?=> F[B]

  // Better inference for applicative syntax
  extension [F[_]: Applicative, A, B](fa: F[A])
    def <*>[C](fb: F[B])(using f: (A, B) => C): F[C] = 
      Applicative[F].map2(fa, fb)(f)

  // Enhanced traverse operations with given instances
  def traverseWith[F[_]: Traverse, G[_]: Applicative, A, B](
      fa: F[A]
  )(f: A => G[B]): G[F[B]] = 
    Traverse[F].traverse(fa)(f)

  def sequenceWith[F[_]: Traverse, G[_]: Applicative, A](
      fga: F[G[A]]
  ): G[F[A]] = 
    Traverse[F].sequence(fga)

  // Better parallel instances
  given [F[_]](using M: Monad[F]): Parallel.Aux[F, F] = 
    new Parallel[F]:
      type F[A] = cats.F[A]
      def applicative: Applicative[F] = M
      def monad: Monad[cats.F] = M
      def sequential: F ~> cats.F = FunctionK.id
      def parallel: cats.F ~> F = FunctionK.id

  // Enhanced Show instances with better inference
  given [A](using s: Show[A]): Conversion[A, String] = s.show

  // Better ordering instances
  given [A](using ord: Order[A]): Ordering[A] = ord.toOrdering
  given [A](using pord: PartialOrder[A]): PartialOrdering[A] = pord.toPartialOrdering

  // Type-level computation helpers for better inference
  object TypeLevel:
    // Better inference for type equality
    transparent inline def =:=[A, B]: A =:= B = 
      summon[A =:= B]

    // Better inference for type constraints  
    transparent inline def <:<[A, B]: A <:< B = 
      summon[A <:< B]

    // Better inference for ClassTag
    transparent inline def classTag[A]: scala.reflect.ClassTag[A] = 
      summon[scala.reflect.ClassTag[A]]

  // Enhanced error handling with better inference
  extension [F[_], E, A](fa: F[A])
    def handleWith[B >: A](f: E => F[B])(using ME: MonadError[F, E]): F[B] =
      ME.handleErrorWith(fa)(f)
    
    def recover[B >: A](f: E => B)(using ME: MonadError[F, E]): F[B] =
      ME.recover(fa)(f)

  // Context-dependent instances for better inference
  given contextualEq[A](using ctx: A): Eq[A] = Eq.fromUniversalEquals

  // Better inference for sum types and ADTs
  extension [A](a: A)
    def narrow[B <: A]: B = a.asInstanceOf[B]
    def widen[B >: A]: B = a

  // Enhanced FunctionK instances with better inference  
  given idFunctionK[F[_]]: FunctionK[F, F] = FunctionK.id[F]
  
  def liftFunctionK[F[_], G[_]](f: [A] => F[A] => G[A]): FunctionK[F, G] =
    new FunctionK[F, G]:
      def apply[A](fa: F[A]): G[A] = f(fa)

  // Better variance handling
  extension [F[+_], A](fa: F[A])
    def upcast[B >: A]: F[B] = fa

  extension [F[-_], A](fa: F[A])  
    def downcast[B <: A]: F[B] = fa

  // Enhanced parallel operations with better inference
  extension [F[_], A](fa: F[A])
    def parMapN[G[_], B, C](fb: F[B])(f: (A, B) => C)(using 
        P: Parallel.Aux[F, G],
        Ap: Applicative[G]
    ): F[C] = 
      P.sequential(Ap.map2(P.parallel(fa), P.parallel(fb))(f))

end GivenInstances 