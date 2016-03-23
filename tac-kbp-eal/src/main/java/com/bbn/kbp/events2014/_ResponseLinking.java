package com.bbn.kbp.events2014;

import com.bbn.bue.common.TextGroupPublicImmutable;
import com.bbn.bue.common.collections.CollectionUtils;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.immutables.func.Functional;
import org.immutables.value.Value;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.concat;

@Value.Immutable
@TextGroupPublicImmutable
@Functional
abstract class _ResponseLinking {
  public abstract Symbol docID();
  public abstract ImmutableSet<ResponseSet> responseSets();
  public abstract ImmutableSet<Response> incompleteResponses();
  public abstract Optional<ImmutableMap<String, ResponseSet>> idsToResponseSets();

  @Value.Check
  protected void checkValidity() {
    // no incomplete response may appear in any response set
    final ImmutableSet<Response> allResponsesInSets = ImmutableSet.copyOf(
        concat(responseSets()));
    for (final Response incompleteResponse : incompleteResponses()) {
      checkArgument(!allResponsesInSets.contains(incompleteResponse),
          "A response may not be both completed and incomplete");
    }
    if (idsToResponseSets().isPresent()) {
      for (final String id : idsToResponseSets().get().keySet()) {
        checkArgument(!id.contains("-"), "Event frame IDs may not contain -s");
      }
      CollectionUtils.assertSameElementsOrIllegalArgument(responseSets(),
          idsToResponseSets().get().values(), "Response sets did not match IDs",
          "Response sets in list", "Response sets in ID map");
    }
  }

  public final ImmutableSet<Response> allResponses() {
    return ImmutableSet.copyOf(concat(concat(responseSets()), incompleteResponses()));
  }

  public final String toString() {
    return MoreObjects.toStringHelper(this)
        .add("docID", docID())
        .add("responseSets", responseSets())
        .add("incomplete", incompleteResponses()).toString();
  }

  public ResponseLinking copyWithFilteredResponses(final Predicate<Response> toKeepCondition) {
    final Set<Response> newIncompletes = Sets.filter(incompleteResponses(), toKeepCondition);
    final ImmutableSet.Builder<ResponseSet> newResponseSetsB = ImmutableSet.builder();
    for (final ResponseSet responseSet : responseSets()) {
      final ImmutableSet<Response> okResponses = FluentIterable.from(responseSet.asSet())
          .filter(toKeepCondition).toSet();
      if (!okResponses.isEmpty()) {
        newResponseSetsB.add(ResponseSet.from(okResponses));
      }
    }

    final ImmutableSet<ResponseSet> newResponseSets = newResponseSetsB.build();
    final ResponseLinking.Builder ret = ResponseLinking.builder().docID(docID())
        .responseSets(newResponseSets).incompleteResponses(newIncompletes);
    if (idsToResponseSets().isPresent()) {
      ret.idsToResponseSets(
              ImmutableMap.copyOf(Maps.filterValues(idsToResponseSets().get(), in(newResponseSets))));
    }
    return ret.build();
  }
}