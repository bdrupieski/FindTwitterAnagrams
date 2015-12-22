package models

import java.util.UUID

import matching.IsSameWhenRearrangedEnum

case class AnagramMatch(id: Int,
                        tweet1Id: UUID,
                        tweet2Id: UUID,
                        editDistanceOriginalText: Int,
                        editDistanceStrippedText: Int,
                        hammingDistanceStrippedText: Int,
                        longestCommonSubstringLengthStrippedText: Int,
                        wordCountDifference: Int,
                        totalUniqueWords: Int,
                        lcsLengthToTotalLengthRatio: Float,
                        editDistanceToLengthRatio: Float,
                        differentWordCountToTotalWordCount: Float,
                        isSameRearranged: IsSameWhenRearrangedEnum.IsSameWhenRearrangedEnum,
                        interestingFactor: Float,
                        posted: Boolean = false,
                        rejected: Boolean = false)
