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

package tech.pegasys.teku.beaconrestapi.handlers.v1.beacon;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_BAD_REQUEST;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_INTERNAL_SERVER_ERROR;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_NOT_FOUND;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_OK;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_UNSUPPORTED_MEDIA_TYPE;
import static tech.pegasys.teku.infrastructure.restapi.MetadataTestUtil.getResponseSszFromMetadata;
import static tech.pegasys.teku.infrastructure.restapi.MetadataTestUtil.getResponseStringFromMetadata;
import static tech.pegasys.teku.infrastructure.restapi.MetadataTestUtil.verifyMetadataErrorResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.beaconrestapi.AbstractMigratedBeaconHandlerWithChainDataProviderTest;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.spec.SpecMilestone;
import tech.pegasys.teku.spec.datastructures.metadata.ObjectAndMetaData;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.versions.electra.BeaconStateElectra;
import tech.pegasys.teku.spec.datastructures.state.versions.electra.PendingPartialWithdrawal;

public class GetStatePendingPartialWithdrawalsTest
    extends AbstractMigratedBeaconHandlerWithChainDataProviderTest {

  @BeforeEach
  public void setup() {

    final GetStatePendingPartialWithdrawals pendingWithdrawalsHandler =
        new GetStatePendingPartialWithdrawals(chainDataProvider, schemaDefinitionCache);
    initialise(SpecMilestone.ELECTRA);
    genesis();
    setHandler(pendingWithdrawalsHandler);
    request.setPathParameter("state_id", "head");
  }

  @Test
  void metadata_shouldHandle400() throws JsonProcessingException {
    verifyMetadataErrorResponse(handler, SC_BAD_REQUEST);
  }

  @Test
  void metadata_shouldHandle404() throws JsonProcessingException {
    verifyMetadataErrorResponse(handler, SC_NOT_FOUND);
  }

  @Test
  void metadata_shouldHandle415() throws JsonProcessingException {
    verifyMetadataErrorResponse(handler, SC_UNSUPPORTED_MEDIA_TYPE);
  }

  @Test
  void metadata_shouldHandle500() throws JsonProcessingException {
    verifyMetadataErrorResponse(handler, SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  void metadata_shouldHandle200() throws IOException {
    final PendingPartialWithdrawal withdrawal = dataStructureUtil.randomPendingPartialWithdrawal();
    final ObjectAndMetaData<List<PendingPartialWithdrawal>> responseData =
        new ObjectAndMetaData<>(List.of(withdrawal), SpecMilestone.ELECTRA, false, true, false);
    final String data = getResponseStringFromMetadata(handler, SC_OK, responseData);
    final String expected =
        String.format(
            "{\"version\":\"electra\",\"execution_optimistic\":false,\"finalized\":false,"
                + "\"data\":[{\"validator_index\":\"%s\",\"amount\":\"%s\",\"withdrawable_epoch\":\"%s\"}]}",
            withdrawal.getValidatorIndex(),
            withdrawal.getAmount(),
            withdrawal.getWithdrawableEpoch());
    assertThat(data).isEqualTo(expected);
  }

  @Test
  void metadata_shouldHandle200OctetStream() throws IOException {
    final BeaconStateElectra state =
        dataStructureUtil.randomBeaconState().toVersionElectra().orElseThrow();
    final PendingPartialWithdrawal pendingPartialWithdrawal =
        dataStructureUtil.randomPendingPartialWithdrawal();
    final SszList<PendingPartialWithdrawal> deposits =
        state.getPendingPartialWithdrawals().getSchema().of(pendingPartialWithdrawal);
    final ObjectAndMetaData<SszList<PendingPartialWithdrawal>> responseData =
        new ObjectAndMetaData<>(deposits, SpecMilestone.ELECTRA, false, true, false);
    final byte[] data = getResponseSszFromMetadata(handler, SC_OK, responseData);
    assertThat(Bytes.of(data)).isEqualTo(deposits.sszSerialize());
  }
}
