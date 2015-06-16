package com.bbn.kbp.events2014.bin.QA.Warnings;

import com.bbn.kbp.events2014.Response;

import com.google.common.collect.ImmutableSet;

/**
 * Created by jdeyoung on 6/15/15.
 */
public final class PronounAsCASWarningRule extends ContainsStringWarningRule {

  private static final ImmutableSet<String> pronouns = ImmutableSet.of("he", "she", "it", "they");

  private PronounAsCASWarningRule(final Iterable<String> verboten) {
    super(verboten);
  }

//  @Override
//  public SetMultimap<Response, Warning> applyWarning(final AnswerKey answerKey) {
//    final SetMultimap<Response, Warning> preliminaryResult = super.applyWarning(answerKey);
//    return null;
//  }

  @Override
  public String getTypeString() {
    return "Prounoun as CAS";
  }

  @Override
  public String getTypeDescription() {
    return "CAS is mostly composed of one of " + pronouns + " maybe it shouldn't be marked";
  }

  public static PronounAsCASWarningRule create() {
    return new PronounAsCASWarningRule(pronouns);
  }

  @Override
  protected boolean warningApplies(final Response input) {
    if(super.warningApplies(input)) {
      if(WHITESPACE_SPLITTER.splitToList(input.canonicalArgument().string()).size() < 3) {
        return false;
      }
      return true;
    }
    return false;
  }
}