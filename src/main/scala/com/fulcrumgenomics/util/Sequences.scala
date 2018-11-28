/**
 * Copyright (c) 2016, Fulcrum Genomics LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.fulcrumgenomics.util

import com.fulcrumgenomics.FgBioDef._
import htsjdk.samtools.util.SequenceUtil


/**
  * Utility methods for working with DNA or RNA sequences
  */
object Sequences {
  /** Common contig/chrom names for non-autosomal sequences in mammals. */
  val CommonNonAutosomalContigNames = Seq("M", "chrM", "MT", "X", "chrX", "Y", "chrY")

  /** Counts the number of mismatches between two sequences of the same length. */
  def countMismatches(s1: String, s2: String): Int = {
    require(s1.length == s2.length, s"Cannot count mismatches in strings of differing lengths: $s1 $s2")

    var count = 0
    forloop (from=0, until=s1.length) { i =>
      val a = Character.toUpperCase(s1.charAt(i))
      val b = Character.toUpperCase(s2.charAt(i))
      if (a != b) count += 1
    }

    count
  }

  /** Class to store the zero-based offset and length of match for various properties of a sequence.  For example, see
    * [[longestHomopolymer]] and [[longestDinuc]]. */
  case class OffsetAndLength(offset: Int, length: Int)

  /**
    * Returns the offset (0-based) and length of the longest homopolymer in the sequence. In the case
    * that there are multiple homopolymers of the same length, the earliest one is returned.
    *
    * @param s a DNA or RNA sequence
    * @return the offset and length of the longest homopolymer
    */
  def longestHomopolymer(s: String) : OffsetAndLength = {
    var (bestStart, bestLength) = (0,0)
    forloop(0, s.length) { start =>
      val firstBase = s.charAt(start).toByte
      var i = start
      while (i < s.length && basesEqual(firstBase, s.charAt(i))) i += 1
      val length = i - start
      if (length > bestLength) {
        bestStart = start
        bestLength = length
      }
    }

    OffsetAndLength(bestStart, bestLength)
  }

  /**
    * Returns the offset (0-based) and length of the longest dinucleotide run in the sequence. In the case
    * that there are multiple dinucleotide runs of the same length, the earliest one is returned.
    *
    * @param s a DNA or RNA sequence
    * @return the offset and length of the longest dinucleotide sequence
    */
  def longestDinuc(s: String) : OffsetAndLength = {
    var (bestStart, bestLength) = (0,0)
    forloop(0, s.length-1) { start =>
      val (b1, b2) = (s.charAt(start).toByte, s.charAt(start+1).toByte)
      var i = start
      while (i < s.length-1 && basesEqual(b1, s.charAt(i)) && basesEqual(b2, s.charAt(i+1))) i += 2
      val length = i - start
      if (length > bestLength) {
        bestStart = start
        bestLength = length
      }
    }

    OffsetAndLength(bestStart, bestLength)
  }

  /** Reverse complements a string of bases. */
  def revcomp(s: String): String = SequenceUtil.reverseComplement(s)

  /** Returns the sequence that is the complement of the provided sequence. */
  def complement(s: String): String = {
    val bs = s.getBytes
    forloop(from=0, until=bs.length) { i =>  bs(i) = SequenceUtil.complement(bs(i)) }
    new String(bs)
  }

  /** Compares if two bases are equal ignoring case.  The bases may be IUPAC codes, but their relationships are not
    * considered. */
  @inline private def basesEqual(b1: Byte, b2: Char): Boolean = SequenceUtil.basesEqual(b1, b2.toByte)
}
