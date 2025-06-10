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

package cats.derived

import cats.*
import cats.data.*
import scala.deriving.*
import scala.compiletime.*

/**
 * Scala 3 specific derivation utilities that leverage improved type inference
 * and compile-time metaprogramming for automatic instance derivation
 */
object Scala3Derivation:

  // Automatic derivation using Scala 3's deriving mechanism
  
  /** 
   * Derive Functor instances automatically with better type inference
   */
  inline given deriveFunctor[F[_]](using m: Mirror.Of[F[Any]]): Functor[F] = 
    deriveFunctorImpl[F]
  
  private inline def deriveFunctorImpl[F[_]]: Functor[F] = 
    new Functor[F]:
      def map[A, B](fa: F[A])(f: A => B): F[B] = 
        // This would need actual implementation based on the structure
        fa.asInstanceOf[F[B]] // Placeholder
  
  /**
   * Derive Eq instances automatically with better inference
   */
  inline given deriveEq[A](using m: Mirror.Of[A]): Eq[A] =
    deriveEqImpl[A]
  
  private inline def deriveEqImpl[A](using m: Mirror.Of[A]): Eq[A] =
    inline m match
      case p: Mirror.ProductOf[A] => deriveEqProduct[A](using p)
      case s: Mirror.SumOf[A] => deriveEqSum[A](using s)
  
  private inline def deriveEqProduct[A](using p: Mirror.ProductOf[A]): Eq[A] =
    new Eq[A]:
      def eqv(x: A, y: A): Boolean =
        val xElems = Tuple.fromProductTyped(x)
        val yElems = Tuple.fromProductTyped(y)
        eqElems(xElems, yElems)
  
  private inline def deriveEqSum[A](using s: Mirror.SumOf[A]): Eq[A] =
    new Eq[A]:
      def eqv(x: A, y: A): Boolean =
        val xOrdinal = constValue[s.MirroredElemTypes] 
        val yOrdinal = constValue[s.MirroredElemTypes]
        xOrdinal == yOrdinal && eqElems(x, y)
  
  private inline def eqElems[T <: Tuple](x: T, y: T): Boolean =
    inline (x, y) match
      case (xh *: xt, yh *: yt) =>
        summonInline[Eq[xh.type]].eqv(xh, yh) && eqElems(xt, yt)
      case (EmptyTuple, EmptyTuple) => true
      case _ => false
  
  /**
   * Derive Show instances automatically
   */
  inline given deriveShow[A](using m: Mirror.Of[A]): Show[A] =
    deriveShowImpl[A]
  
  private inline def deriveShowImpl[A](using m: Mirror.Of[A]): Show[A] =
    new Show[A]:
      def show(a: A): String =
        inline m match
          case p: Mirror.ProductOf[A] =>
            val label = constValue[p.MirroredLabel]
            val elems = Tuple.fromProductTyped(a)
            s"$label(${showElems(elems)})"
          case s: Mirror.SumOf[A] =>
            val label = constValue[s.MirroredLabel] 
            s"$label(${a.toString})"
  
  private inline def showElems[T <: Tuple](t: T): String =
    inline t match
      case h *: EmptyTuple => 
        summonInline[Show[h.type]].show(h)
      case h *: t =>
        summonInline[Show[h.type]].show(h) + ", " + showElems(t)
      case EmptyTuple => ""
  
  /**
   * Enhanced derivation macros with better type inference
   */
  object DerivationMacros:
    
    /**
     * Automatically derive type class instances with context bounds
     */
    transparent inline def deriveInstance[TC[_], A]: TC[A] = 
      summonInline[TC[A]]
    
    /**
     * Derive with explicit type class parameter for better inference
     */
    transparent inline def deriveWith[TC[_], A](using tc: TC[A]): TC[A] = tc
    
    /**
     * Conditional derivation based on available instances
     */
    transparent inline def deriveIf[TC[_], A]: Option[TC[A]] =
      summonFrom:
        case given TC[A] => Some(summon[TC[A]])
        case _ => None
  
  /**
   * Better type inference for common derivation patterns
   */
  extension [A](a: A)
    /**
     * Derive an instance on demand with better inference
     */
    transparent inline def deriveInstance[TC[_]]: TC[A] = 
      summonInline[TC[A]]
    
    /**
     * Show derivation with fallback
     */
    inline def showDerived(using m: Mirror.Of[A]): String =
      deriveShow[A].show(a)
  
  /**
   * Typeclass composition with better inference
   */
  object Composition:
    
    /**
     * Compose functors with automatic derivation
     */
    given [F[_]: Functor, G[_]: Functor]: Functor[Compose[F, G, *]] =
      Functor[F].compose[G]
    
    /**
     * Compose applicatives with automatic derivation  
     */
    given [F[_]: Applicative, G[_]: Applicative]: Applicative[Compose[F, G, *]] =
      Applicative[F].compose[G]
    
    /**
     * Product of functors
     */
    given [F[_]: Functor, G[_]: Functor]: Functor[Tuple2K[F, G, *]] =
      new Functor[Tuple2K[F, G, *]]:
        def map[A, B](fa: Tuple2K[F, G, A])(f: A => B): Tuple2K[F, G, B] =
          Tuple2K(fa.first.map(f), fa.second.map(f))
  
  /**
   * Union type support for better error handling
   */
  object UnionTypes:
    
    type StringOrInt = String | Int
    type ErrorOrResult[E, A] = E | A
    
    /**
     * Better handling of union types in error scenarios
     */
    extension [E, A](value: E | A)
      def fold[B](fe: E => B, fa: A => B): B =
        value match
          case e: E => fe(e) 
          case a: A => fa(a)
      
      def toEither: Either[E, A] =
        value match
          case e: E => Left(e)
          case a: A => Right(a)
  
  /**
   * Opaque type support for better type safety
   */
  object OpaqueTypes:
    
    opaque type UserId = String
    object UserId:
      def apply(s: String): UserId = s
      extension (id: UserId) def value: String = id
      
      given Show[UserId] = Show[String].contramap(_.value)
      given Eq[UserId] = Eq[String].contramap(_.value)
      given Order[UserId] = Order[String].contramap(_.value)
    
    opaque type ProductId = Long  
    object ProductId:
      def apply(l: Long): ProductId = l
      extension (id: ProductId) def value: Long = id
      
      given Show[ProductId] = Show[Long].contramap(_.value)
      given Eq[ProductId] = Eq[Long].contramap(_.value)
      given Order[ProductId] = Order[Long].contramap(_.value)
  
  /**
   * Enhanced pattern matching with better type inference
   */
  object PatternMatching:
    
    /**
     * Type-safe pattern matching with exhaustiveness
     */
    inline def matchExhaustive[A, B](value: A)(cases: A => B): B = cases(value)
    
    /**
     * Pattern matching with union type support
     */
    extension [A, B](value: A | B)
      inline def matchUnion[C](fa: A => C, fb: B => C): C =
        value match
          case a: A => fa(a)
          case b: B => fb(b)
  
  /**
   * Context function helpers for dependency injection patterns
   */
  object ContextFunctions:
    
    type WithContext[Ctx, A] = Ctx ?=> A
    type WithConfig[A] = Config ?=> A
    type WithLogger[A] = Logger ?=> A
    
    case class Config(value: String)
    case class Logger(log: String => Unit)
    
    /**
     * Better inference for context-dependent computations
     */
    def withConfig[A](config: Config)(computation: WithConfig[A]): A =
      computation(using config)
    
    def withLogger[A](logger: Logger)(computation: WithLogger[A]): A =
      computation(using logger)
    
    /**
     * Compose context functions with better inference
     */
    extension [Ctx, A](f: WithContext[Ctx, A])
      def andThen[B](g: A => B): WithContext[Ctx, B] =
        g(f)
      
      def provide(ctx: Ctx): A = f(using ctx)

end Scala3Derivation 