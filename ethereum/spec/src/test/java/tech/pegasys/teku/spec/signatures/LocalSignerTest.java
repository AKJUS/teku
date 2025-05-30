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

package tech.pegasys.teku.spec.signatures;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.bls.BLSTestUtil;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.async.StubAsyncRunner;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.TestSpecFactory;
import tech.pegasys.teku.spec.datastructures.blocks.BeaconBlock;
import tech.pegasys.teku.spec.datastructures.builder.ValidatorRegistration;
import tech.pegasys.teku.spec.datastructures.operations.AggregateAndProof;
import tech.pegasys.teku.spec.datastructures.operations.AttestationData;
import tech.pegasys.teku.spec.datastructures.operations.VoluntaryExit;
import tech.pegasys.teku.spec.datastructures.state.ForkInfo;
import tech.pegasys.teku.spec.util.DataStructureUtil;

// Note: Changes to the order items are created in DataStructureUtil are created will often cause
// failures in this test. If there are no changes to signing code, just update the expected result.
class LocalSignerTest {
  private final Spec spec = TestSpecFactory.createMinimalPhase0();
  private final Spec denebSpec = TestSpecFactory.createMinimalDeneb();
  private final DataStructureUtil dataStructureUtil = new DataStructureUtil(spec);
  private final DataStructureUtil dataStructureUtilDeneb = new DataStructureUtil(denebSpec);

  private final ForkInfo fork = dataStructureUtil.randomForkInfo();

  private static final BLSKeyPair KEYPAIR = BLSTestUtil.randomKeyPair(1234);
  private final StubAsyncRunner asyncRunner = new StubAsyncRunner();

  private final LocalSigner signer = new LocalSigner(spec, KEYPAIR, asyncRunner);

  @Test
  public void shouldSignBlock() {
    final BeaconBlock block = dataStructureUtil.randomBeaconBlock(10);
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "o6/J6sxThPzADTjmP8T6tuJvG+Rll8usMxCCbq5sMItDeblgqsPXgbPihBDcJo0pBEpPZec1uPPd9rNK9O4uKhIuXw2rKzFcYzLvOjS50kpNHVQ42HN8gUmcmvC06tLo"));

    final SafeFuture<BLSSignature> result = signer.signBlock(block, fork);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignBlindedBlock() {
    final BeaconBlock block = dataStructureUtil.randomBlindedBeaconBlock(10);
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "o2ekZJkMs5GPMFR7sVgIf/ikm9QATJRSnXCk8AR/58c/t6C4H5RJKNhU662xS6XBC2i/I0PF4HaTOV4gX8pNMWcHnGB06ZHTSWQ5NFwnhEMQ3GLO2/BYYd5arJkVp/Tc"));

    final SafeFuture<BLSSignature> result = signer.signBlock(block, fork);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldCreateRandaoReveal() {
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "j7vOT7GQBnv+aIqxb0byMWNvMCXhQwAfj38UcMne7pNGXOvNZKnXQ9Knma/NOPUyAvLcRBDtew23vVtzWcm7naaTRJVvLJS6xiPOMIHOw6wNtGggzc20heZAXZAMdaKi"));
    final SafeFuture<BLSSignature> reveal = signer.createRandaoReveal(UInt64.valueOf(7), fork);
    asyncRunner.executeQueuedActions();

    assertThat(reveal)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            reveal.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignAttestationData() {
    final AttestationData attestationData = dataStructureUtil.randomAttestationData();
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "l1DUv3fmbvZanhCaaraMk2PKAl+33sf3UHMbxkv18CKILzzIz+Hr6hnLXCHqWQYEGKTtLcf6OLV7Z+Y21BW2bBtJHXJqqzvWkec/j0X0hWaEoWOSAs20sipO1WSIUY2m"));

    final SafeFuture<BLSSignature> result = signer.signAttestationData(attestationData, fork);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignAggregationSlot() {
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "hnCLCZlbEyzMFq2JLHl6wk4W6gpbFGoQA2N4WB+CpgqVg3gcxJpRKOswtSTU4XdSEU2x3Hf0oTlxer/gVaFwAh84Mm4VLH67LNUxVO4+o2Q5TxOD1sArnvMcOJdGMGp2"));

    final SafeFuture<BLSSignature> result = signer.signAggregationSlot(UInt64.valueOf(7), fork);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignAggregateAndProof() {
    final AggregateAndProof aggregateAndProof = dataStructureUtil.randomAggregateAndProof();
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromHexString(
                "0xb80875d8c01900883874ff5dff6c0f2b00f0ba3841c82e787712d8fc5fcb98a673f4150fff04c17816bab09c9f8265a11151c9ed9800cc6a0511e016fd5a7753574571889bd454de641e169a2d00dd7e72a2a06088faf77cffd575e2f8c90019"));

    final SafeFuture<BLSSignature> result = signer.signAggregateAndProof(aggregateAndProof, fork);
    asyncRunner.executeQueuedActions();

    assertThat(result).isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignVoluntaryExit() {
    final VoluntaryExit voluntaryExit = dataStructureUtil.randomVoluntaryExit();
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "qumUYTSgi8hmsS3/a1oDLGSIfOQ4+PgByZpDprnvlQKaDTXnlzGQloQ/W0kAeMa8EhXvGvF0OiGkQxEEznpVsFNhZ01H+3S2StWqq7S0mbRcbJhT6fEcyrOMqRer36q8"));

    final SafeFuture<BLSSignature> result = signer.signVoluntaryExit(voluntaryExit, fork);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignVoluntaryExitWithCapellaForkDomainAfterDeneb() {
    final VoluntaryExit voluntaryExit = dataStructureUtilDeneb.randomVoluntaryExit();
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "j08XcEXfa3g/D/F8lYUmkFS2ZXcnfW82Y0lGA5jnnWmBB/1ypPtsEEwvAO3DpXNnCRSCxVUhJFqUa1krAuxTgZZofyvakpLM/EA5Nqe0Rq8Epk7uPfX5fPiEg/1vJFn9"));

    final LocalSigner signer = new LocalSigner(denebSpec, KEYPAIR, asyncRunner);
    final SafeFuture<BLSSignature> result = signer.signVoluntaryExit(voluntaryExit, fork);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }

  @Test
  public void shouldSignValidatorRegistration() {
    final ValidatorRegistration validatorRegistration =
        dataStructureUtil.randomValidatorRegistration();
    final BLSSignature expectedSignature =
        BLSSignature.fromBytesCompressed(
            Bytes.fromBase64String(
                "pTYaqzqFTKb4bOX8kc8vEFj6z/eLbYH9+uGeFFxtklCUlPqugzAQyc7y/8KPcBPJBzRv5Knuph2wnGIyY2c0YbQzblvfXlPGjhBMhL/t8iaS4uF5mYvrZDKefXoNF9TB"));

    final SafeFuture<BLSSignature> result = signer.signValidatorRegistration(validatorRegistration);
    asyncRunner.executeQueuedActions();

    assertThat(result)
        .withFailMessage(
            "expected: %s\nbut was: %s",
            expectedSignature.toBytesCompressed().toBase64String(),
            result.getImmediately().toBytesCompressed().toBase64String())
        .isCompletedWithValue(expectedSignature);
  }
}
