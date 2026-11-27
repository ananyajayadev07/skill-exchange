package com.example.skill_exchange;

import java.util.Set;

/** A mutual match: score = number of skills that line up in both directions. */
public record MatchResult(Student student,
                          int score,
                          Set<String> theyCanTeachYou,
                          Set<String> youCanTeachThem) {
}
