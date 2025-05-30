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

package tech.pegasys.teku.spec.datastructures.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.TestSpecFactory;
import tech.pegasys.teku.spec.util.DataStructureUtil;

class DepositDataTest {
  private final DataStructureUtil dataStructureUtil =
      new DataStructureUtil(TestSpecFactory.createDefault());
  private final BLSPublicKey pubkey = dataStructureUtil.randomPublicKey();
  private final Bytes32 withdrawalCredentials = dataStructureUtil.randomBytes32();
  private final UInt64 amount = dataStructureUtil.randomUInt64();
  private final BLSSignature signature = dataStructureUtil.randomSignature();

  private final DepositData depositData =
      new DepositData(pubkey, withdrawalCredentials, amount, signature);

  @Test
  void equalsReturnsTrueWhenObjectsAreSame() {
    DepositData testDepositInput = depositData;

    assertEquals(depositData, testDepositInput);
  }

  @Test
  void equalsReturnsTrueWhenObjectFieldsAreEqual() {
    DepositData testDepositInput =
        new DepositData(pubkey, withdrawalCredentials, amount, signature);

    assertEquals(depositData, testDepositInput);
  }

  @Test
  void equalsReturnsFalseWhenPubkeysAreDifferent() {
    BLSPublicKey differentPublicKey = dataStructureUtil.randomPublicKey();
    DepositData testDepositInput =
        new DepositData(differentPublicKey, withdrawalCredentials, amount, signature);

    assertNotEquals(pubkey, differentPublicKey);
    assertNotEquals(depositData, testDepositInput);
  }

  @Test
  void equalsReturnsFalseWhenWithdrawalCredentialsAreDifferent() {
    DepositData testDepositInput =
        new DepositData(pubkey, withdrawalCredentials.not(), amount, signature);

    assertNotEquals(depositData, testDepositInput);
  }

  @Test
  void equalsReturnsFalseWhenProofsOfPossessionAreDifferent() {
    BLSSignature differentSignature = dataStructureUtil.randomSignature();
    DepositData testDepositInput =
        new DepositData(pubkey, withdrawalCredentials, amount, differentSignature);

    assertNotEquals(signature, differentSignature);
    assertNotEquals(depositData, testDepositInput);
  }

  @Test
  void roundtripSSZ() {
    assertEquals(depositData, DepositData.SSZ_SCHEMA.sszDeserialize(depositData.sszSerialize()));
  }
}
