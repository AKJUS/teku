/*
 * Copyright Consensys Software Inc., 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.validator.client.duties.attestations;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.operations.AttestationData;
import tech.pegasys.teku.validator.client.Validator;

public class AggregatorsGroupedByCommittee implements AggregationDutyAggregators {

  private final ConcurrentMap<Integer, CommitteeAggregator> aggregatorsByCommitteeIndex =
      new ConcurrentHashMap<>();

  @Override
  public void addValidator(
      final Validator validator,
      final int validatorIndex,
      final BLSSignature proof,
      final int attestationCommitteeIndex,
      final SafeFuture<Optional<AttestationData>> unsignedAttestationFuture) {
    aggregatorsByCommitteeIndex.computeIfAbsent(
        attestationCommitteeIndex,
        committeeIndex ->
            new CommitteeAggregator(
                validator,
                UInt64.valueOf(validatorIndex),
                UInt64.valueOf(attestationCommitteeIndex),
                proof,
                unsignedAttestationFuture));
  }

  @Override
  public boolean hasAggregators() {
    return !aggregatorsByCommitteeIndex.isEmpty();
  }

  @Override
  public Stream<CommitteeAggregator> streamAggregators() {
    return aggregatorsByCommitteeIndex.values().stream();
  }

  @VisibleForTesting
  Map<Integer, CommitteeAggregator> getAggregatorsByCommitteeIndex() {
    return aggregatorsByCommitteeIndex;
  }
}
