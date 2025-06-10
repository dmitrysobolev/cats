# Scala 3 Type Inference Optimizations

This guide covers the Scala 3 specific optimizations in Cats that leverage improved type inference, new syntax features, and enhanced metaprogramming capabilities.

## Overview

Scala 3 introduces significant improvements to type inference, new syntax features, and enhanced metaprogramming that Cats can leverage to provide better developer experience:

- **Using/Given clauses** for cleaner implicit resolution
- **Extension methods** for better syntax
- **Union types** for improved error handling  
- **Opaque types** for better type safety
- **Context functions** for dependency injection
- **Improved derivation** with compile-time metaprogramming
- **Polymorphic function types** for better abstractions

## Key Features

### 1. Enhanced Given Instances

```scala
import cats.GivenInstances.*

// Automatic instance resolution with better inference
val result: Option[String] = Some("hello").map(_.toUpperCase)
// No need for explicit imports - given instances provide better inference

// Better Either handling
val either: Either[String, Int] = Right(42)
val mapped = either.map(_ * 2) // Type inference just works
```

### 2. Improved Extension Methods

```scala
import cats.syntax.scala3TypeInference.*

// Enhanced traverse with polymorphic functions
val list = List(1, 2, 3)
val result = list.traverseP[Option, Int]([X] => (x: X) => Some(x))

// Better applicative syntax
val opt1 = Some(1)
val opt2 = Some(2) 
val combined = opt1.applyTuple(opt2)(_ + _)

// Context function support
def computation(using config: Config): Option[String] = 
  Some(config.value.toUpperCase)

val result = Some("test").flatMapCtx(computation)
```

### 3. Union Type Error Handling

```scala
import cats.derived.Scala3Derivation.UnionTypes.*

type MyError = String | Int | RuntimeException

// Better error composition
def riskyOperation(): Either[MyError, String] = {
  val error: MyError = "Something went wrong"
  Left(error)
}

// Pattern matching with union types
val result = riskyOperation()
result.leftMap(_.fold(
  str => s"String error: $str",
  int => s"Int error: $int", 
  ex => s"Exception: ${ex.getMessage}"
))
```

### 4. Automatic Derivation

```scala
import cats.derived.Scala3Derivation.*

case class Person(name: String, age: Int) derives Show, Eq

// Automatic derivation with better inference
val person = Person("Alice", 30)
println(person.show) // Person(Alice, 30)

// Conditional derivation
val showInstance = DerivationMacros.deriveIf[Show, Person]
// Returns Some(Show[Person]) if derivable, None otherwise
```

### 5. Opaque Types for Type Safety

```scala
import cats.derived.Scala3Derivation.OpaqueTypes.*

val userId = UserId("user123")
val productId = ProductId(456L)

// Type safety - won't compile
// val invalid: UserId = productId ❌

// Automatic instances
println(userId.show) // Uses derived Show
```

### 6. Context Functions

```scala
import cats.derived.Scala3Derivation.ContextFunctions.*

case class DatabaseConfig(url: String)
case class Logger(log: String => Unit)

// Context function types
type WithDB[A] = DatabaseConfig ?=> A
type WithLog[A] = Logger ?=> A

def fetchUser(id: String): WithDB[WithLog[Option[User]]] =
  val config = summon[DatabaseConfig]
  val logger = summon[Logger]
  logger.log(s"Fetching user $id from ${config.url}")
  // ... database logic
  None

// Usage with automatic context propagation
given DatabaseConfig = DatabaseConfig("jdbc:...")
given Logger = Logger(println)

val user = fetchUser("123") // Context automatically provided
```

### 7. Enhanced Monad Operations

```scala
import cats.syntax.MonadSyntax3.*

// Better inference for monadic operations
val result = for {
  x <- Option(1)
  y <- Option(2)
} yield x + y

// Context function support
def withContext(using ctx: String): Option[String] = Some(ctx.toUpperCase)
val contextResult = Some("hello").flatMapCtx(withContext)

// Union type error handling
val unionResult: Option[String | Int] = Some("hello").toUnion[Int]

// Better parallel operations
val par1 = Option(1)
val par2 = Option(2)
val parResult = par1.parMap2(par2)(_ + _)
```

### 8. Better Type Class Composition

```scala
import cats.derived.Scala3Derivation.Composition.*

// Automatic composition instances
val composed: Compose[Option, List, String] = 
  Compose(Some(List("a", "b", "c")))

val mapped = composed.map(_.toUpperCase) // Type inference works seamlessly

// Product types
val product: Tuple2K[Option, List, Int] = 
  Tuple2K(Some(1), List(1, 2, 3))
```

## Migration Guide

### From Scala 2 Implicit Syntax

**Before (Scala 2):**
```scala
def process[F[_]: Monad](fa: F[String]): F[String] = 
  fa.flatMap(s => Monad[F].pure(s.toUpperCase))
```

**After (Scala 3):**
```scala
def process[F[_]: Monad](fa: F[String]): F[String] = 
  fa.flatMap(s => summon[Monad[F]].pure(s.toUpperCase))

// Or with using syntax
def processUsing[F[_]](fa: F[String])(using M: Monad[F]): F[String] = 
  fa.flatMap(s => M.pure(s.toUpperCase))
```

### Better Error Handling

**Before:**
```scala
def parse(s: String): Either[String, Int] = 
  try Right(s.toInt) catch case _ => Left("Not a number")
```

**After:**
```scala
type ParseError = String | NumberFormatException

def parse(s: String): Either[ParseError, Int] = 
  try Right(s.toInt) 
  catch case e: NumberFormatException => Left(e)
```

## Performance Benefits

### Compile-Time Optimizations

1. **Better Inline Expansion**: Transparent inline methods reduce runtime overhead
2. **Improved Specialization**: @sp annotations work better with new inference
3. **Reduced Boxing**: Union types can reduce boxing in some scenarios
4. **Better Dead Code Elimination**: Compiler can optimize unused branches better

### Runtime Optimizations

1. **Reduced Allocations**: Extension methods avoid wrapper classes
2. **Better JIT Optimization**: Cleaner bytecode from improved inference
3. **Faster Implicit Resolution**: Given/using has lower overhead than implicit search

## Best Practices

### 1. Use Extension Methods Over Implicit Classes

**Preferred:**
```scala
extension [F[_]: Functor, A](fa: F[A])
  def mapTwice[B](f: A => B): F[B] = fa.map(f).map(f)
```

**Avoid:**
```scala
implicit class FunctorOps[F[_]: Functor, A](fa: F[A]) {
  def mapTwice[B](f: A => B): F[B] = fa.map(f).map(f)
}
```

### 2. Leverage Context Functions for DI

```scala
// Clean dependency injection
type WithServices[A] = (Database, Logger) ?=> A

def businessLogic: WithServices[Either[Error, Result]] = {
  val db = summon[Database]
  val logger = summon[Logger]
  // ... business logic
}
```

### 3. Use Union Types for Error ADTs

```scala
// Instead of sealed trait hierarchies for simple errors
type ServiceError = ValidationError | DatabaseError | NetworkError

def service(): Either[ServiceError, Data] = ???
```

### 4. Prefer Opaque Types for Domain Models

```scala
opaque type UserId = String
opaque type Email = String

// Automatic derivation of type class instances
object UserId:
  def apply(s: String): UserId = s
  given Show[UserId] = Show[String].contramap(identity)
```

## Integration with Existing Code

The Scala 3 optimizations are designed to be **fully backward compatible**. You can:

1. **Gradually migrate** from implicit to using/given syntax
2. **Mix extension methods** with existing implicit classes
3. **Use union types** alongside Either for gradual adoption
4. **Keep existing instances** while adding given instances

## Future Enhancements

Planned improvements include:

1. **Match Types** for more precise type computations
2. **Metaprogramming** for even better derivation
3. **Better HKT Inference** with improved type system features
4. **Enhanced Parallel Operations** with union type integration

## Examples in Action

Check out the test files and benchmarks to see these optimizations in action:

- `tests/shared/src/test/scala/cats/tests/Scala3TypeInferenceTests.scala`
- `bench/src/main/scala/cats/bench/Scala3InferenceBench.scala`

These demonstrate real-world usage patterns and performance characteristics of the new features. 