/*
 * Copyright 2020 Daniel Spiewak
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

package shims.conversions

import scalaz.\/

import shims.AsSyntax
import shims.util.Capture

trait ComposeConversions {

  private[conversions] trait ComposeShimS2C[F[_, _]] extends cats.arrow.Compose[F] with Synthetic {
    val F: scalaz.Compose[F]

    override def compose[A, B, C](f: F[B, C], g: F[A, B]): F[A, C] = F.compose(f, g)
  }

  implicit def composeToCats[F[_, _]](implicit FC: Capture[scalaz.Compose[F]]): cats.arrow.Compose[F] with Synthetic =
    new ComposeShimS2C[F] { val F = FC.value }

  private[conversions] trait ComposeShimC2S[F[_, _]] extends scalaz.Compose[F] with Synthetic {
    val F: cats.arrow.Compose[F]

    override def compose[A, B, C](f: F[B, C], g: F[A, B]): F[A, C] = F.compose(f, g)
  }

  implicit def composeToScalaz[F[_, _]](implicit FC: Capture[cats.arrow.Compose[F]]): scalaz.Compose[F] with Synthetic =
    new ComposeShimC2S[F] { val F = FC.value }
}

trait ProfunctorConversions {

  private[conversions] trait ProfunctorShimS2C[F[_, _]] extends cats.arrow.Profunctor[F] with Synthetic {
    val F: scalaz.Profunctor[F]

    override def dimap[A, B, C, D](fab: F[A, B])(f: C => A)(g: B => D): F[C, D] = F.dimap(fab)(f)(g)
  }

  implicit def profunctorToCats[F[_, _]](implicit FC: Capture[scalaz.Profunctor[F]]): cats.arrow.Profunctor[F] with Synthetic =
    new ProfunctorShimS2C[F] { val F = FC.value }

  private[conversions] trait ProfunctorShimC2S[F[_, _]] extends scalaz.Profunctor[F] with Synthetic {
    val F: cats.arrow.Profunctor[F]

    override def mapfst[A, B, C](fab: F[A, B])(f: C => A): F[C, B] = F.lmap(fab)(f)

    override def mapsnd[A, B, C](fab: F[A, B])(f: B => C): F[A, C] = F.rmap(fab)(f)
  }

  implicit def profunctorToScalaz[F[_, _]](implicit FC: Capture[cats.arrow.Profunctor[F]]): scalaz.Profunctor[F] with Synthetic =
    new ProfunctorShimC2S[F] { val F = FC.value }
}

trait StrongConversions extends ProfunctorConversions {

  private[conversions] trait StrongShimS2C[F[_, _]] extends cats.arrow.Strong[F] with ProfunctorShimS2C[F] {
    val F: scalaz.Strong[F]

    override def first[A, B, C](fa: F[A, B]): F[(A, C), (B, C)] = F.first(fa)

    override def second[A, B, C](fa: F[A, B]): F[(C, A), (C, B)] = F.second(fa)
  }

  implicit def strongToCats[F[_, _]](implicit FC: Capture[scalaz.Strong[F]]): cats.arrow.Strong[F] with Synthetic =
    new StrongShimS2C[F] { val F = FC.value }

  private[conversions] trait StrongShimC2S[F[_, _]] extends scalaz.Strong[F] with ProfunctorShimC2S[F] {
    val F: cats.arrow.Strong[F]

    override def first[A, B, C](fa: F[A, B]): F[(A, C), (B, C)] = F.first(fa)

    override def second[A, B, C](fa: F[A, B]): F[(C, A), (C, B)] = F.second(fa)
  }

  implicit def strongToScalaz[F[_, _]](implicit FC: Capture[cats.arrow.Strong[F]]): scalaz.Strong[F] with Synthetic =
    new StrongShimC2S[F] { val F = FC.value }
}

trait CategoryConversions extends ComposeConversions {

  private[conversions] trait CategoryShimS2C[F[_, _]] extends cats.arrow.Category[F] with ComposeShimS2C[F] {
    val F: scalaz.Category[F]

    override def id[A]: F[A, A] = F.id
  }

  implicit def categoryToCats[F[_, _]](implicit FC: Capture[scalaz.Category[F]]): cats.arrow.Category[F] with Synthetic =
    new CategoryShimS2C[F] { val F = FC.value }

  private[conversions] trait CategoryShimC2S[F[_, _]] extends scalaz.Category[F] with ComposeShimC2S[F] {
    val F: cats.arrow.Category[F]

    override def id[A]: F[A, A] = F.id
  }

  implicit def categoryToScalaz[F[_, _]](implicit FC: Capture[cats.arrow.Category[F]]): scalaz.Category[F] with Synthetic =
    new CategoryShimC2S[F] { val F = FC.value }
}

trait ArrowConversions extends StrongConversions with CategoryConversions {

  private[conversions] trait ArrowShimS2C[F[_, _]] extends cats.arrow.Arrow[F] with StrongShimS2C[F] with CategoryShimS2C[F] {
    val F: scalaz.Arrow[F]

    override def lift[A, B](f: A => B): F[A, B] = F.arr(f)
    override def split[A, B, C, D](f: F[A, B], g: F[C, D]): F[(A, C), (B, D)] = F.split(f, g)
  }

  implicit def arrowToCats[F[_, _]](implicit FC: Capture[scalaz.Arrow[F]]): cats.arrow.Arrow[F] with Synthetic =
    new ArrowShimS2C[F] { val F = FC.value }

  private[conversions] trait ArrowShimC2S[F[_, _]] extends scalaz.Arrow[F] with ComposeShimC2S[F] with StrongShimC2S[F] with CategoryShimC2S[F] {
    val F: cats.arrow.Arrow[F]

    override def arr[A, B](f: A => B): F[A, B] = F.lift(f)
  }

  implicit def arrowToScalaz[F[_, _]](implicit FC: Capture[cats.arrow.Arrow[F]]): scalaz.Arrow[F] with Synthetic =
    new ArrowShimC2S[F] { val F = FC.value }
}

trait ChoiceConversions extends CategoryConversions with EitherConverters with ArrowConversions {

  private[conversions] trait ChoiceShimS2C[F[_, _]] extends cats.arrow.Choice[F] with CategoryShimS2C[F] {
    val F: scalaz.Choice[F]
    val Bifunctor: scalaz.Bifunctor[F]

    override def choice[A, B, C](f: F[A, C], g: F[B, C]): F[Either[A, B], C] =
      Bifunctor.leftMap(F.choice(f, g))(_.asCats)
  }

  // the bifunctor is required because cats/scalaz use different Either types :-/
  implicit def choiceToCats[F[_, _]](implicit FC: Capture[scalaz.Choice[F]], BF: scalaz.Bifunctor[F]): cats.arrow.Choice[F] with Synthetic =
    new ChoiceShimS2C[F] { val F = FC.value; val Bifunctor = BF }

  private[conversions] trait ChoiceShimC2S[F[_, _]] extends scalaz.Choice[F] with CategoryShimC2S[F] {
    val F: cats.arrow.Choice[F]
    val Bifunctor: cats.Bifunctor[F]

    override def choice[A, B, C](f: => F[A, C], g: => F[B, C]): F[A \/ B, C] =
      Bifunctor.leftMap(F.choice(f, g))(_.asScalaz)
  }

  // the bifunctor is required because cats/scalaz use different Either types :-/
  implicit def choiceToScalaz[F[_, _]](implicit FC: Capture[cats.arrow.Choice[F]], BF: cats.Bifunctor[F]): scalaz.Choice[F] with Synthetic =
    new ChoiceShimC2S[F] { val F = FC.value; val Bifunctor = BF }
}
