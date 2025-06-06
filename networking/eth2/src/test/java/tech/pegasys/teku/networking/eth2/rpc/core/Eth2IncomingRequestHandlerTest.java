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

package tech.pegasys.teku.networking.eth2.rpc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.async.Waiter;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.networking.eth2.rpc.Utils;
import tech.pegasys.teku.networking.eth2.rpc.beaconchain.BeaconChainMethods;
import tech.pegasys.teku.networking.eth2.rpc.core.encodings.RpcEncoding;
import tech.pegasys.teku.networking.eth2.rpc.core.methods.Eth2RpcMethod;
import tech.pegasys.teku.spec.TestSpecFactory;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.spec.datastructures.networking.libp2p.rpc.BeaconBlocksByRangeRequestMessage;
import tech.pegasys.teku.spec.datastructures.networking.libp2p.rpc.EmptyMessage;
import tech.pegasys.teku.spec.datastructures.networking.libp2p.rpc.metadata.MetadataMessage;

public class Eth2IncomingRequestHandlerTest
    extends AbstractRequestHandlerTest<
        Eth2IncomingRequestHandler<BeaconBlocksByRangeRequestMessage, SignedBeaconBlock>> {

  private final BeaconBlocksByRangeRequestMessage request =
      new BeaconBlocksByRangeRequestMessage(UInt64.ONE, UInt64.ONE, UInt64.ONE);

  private Bytes requestData;

  @BeforeEach
  @Override
  public void setup() {
    super.setup();
    requestData = beaconChainMethods.beaconBlocksByRange().encodeRequest(request);

    lenient()
        .when(combinedChainDataClient.getBlockAtSlotExact(any(), any()))
        .thenAnswer(i -> getBlockAtSlot(i.getArgument(0)));
    when(peer.approveRequest()).thenReturn(true);
  }

  @Override
  protected Eth2IncomingRequestHandler<BeaconBlocksByRangeRequestMessage, SignedBeaconBlock>
      createRequestHandler(final BeaconChainMethods beaconChainMethods) {
    final Eth2RpcMethod<BeaconBlocksByRangeRequestMessage, SignedBeaconBlock> method =
        beaconChainMethods.beaconBlocksByRange();
    return method.createIncomingRequestHandler(method.getIds().get(0));
  }

  private SafeFuture<Optional<SignedBeaconBlock>> getBlockAtSlot(final UInt64 slot) {
    final SignedBeaconBlock block = dataStructureUtil.randomSignedBeaconBlock(slot.longValue());
    return SafeFuture.completedFuture(Optional.of(block));
  }

  @Test
  public void testEmptyRequestMessage() {
    final Eth2RpcMethod<EmptyMessage, MetadataMessage> method = beaconChainMethods.getMetadata();
    Eth2IncomingRequestHandler<EmptyMessage, MetadataMessage> requestHandler =
        method.createIncomingRequestHandler(method.getIds().get(0));
    requestHandler.readComplete(nodeId, rpcStream);
    asyncRunner.executeQueuedActions();
    // verify non-error response
    verify(rpcStream).writeBytes(argThat(bytes -> bytes.get(0) == 0));
    verify(rpcStream).closeAbruptly();
  }

  @Test
  public void shouldCloseStreamIfRequestNotReceivedInTime() {
    verify(rpcStream, never()).closeAbruptly();
    verify(rpcStream, never()).closeWriteStream();

    // When timeout completes, we should close stream
    assertThat(asyncRunner.countDelayedActions()).isEqualTo(1);
    asyncRunner.executeQueuedActions();

    verify(rpcStream).closeAbruptly();
    verify(rpcStream, never()).closeWriteStream();
  }

  @Test
  public void shouldNotCloseStreamIfRequestReceivedInTime() throws Exception {
    // Deliver request and wait for it to be processed
    reqHandler.processData(nodeId, rpcStream, Utils.toByteBuf(requestData));
    Waiter.waitFor(() -> assertThat(reqHandler.hasRequestBeenReceived()).isTrue());

    // When timeout completes, we should not close stream
    assertThat(asyncRunner.countDelayedActions()).isEqualTo(1);
    asyncRunner.executeQueuedActions();
    asyncRunner.executeQueuedActions();
    verify(rpcStream, never()).closeAbruptly();
  }

  @Override
  protected RpcEncoding getRpcEncoding() {
    return RpcEncoding.createSszSnappyEncoding(
        TestSpecFactory.createDefault().getNetworkingConfig().getMaxPayloadSize());
  }
}
