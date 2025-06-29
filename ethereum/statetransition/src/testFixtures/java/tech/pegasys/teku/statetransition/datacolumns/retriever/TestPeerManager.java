/*
 * Copyright Consensys Software Inc., 2024
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

package tech.pegasys.teku.statetransition.datacolumns.retriever;

import java.util.HashMap;
import java.util.Map;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.spec.datastructures.blobs.versions.fulu.DataColumnSidecar;
import tech.pegasys.teku.spec.datastructures.util.DataColumnSlotAndIdentifier;

public class TestPeerManager implements DataColumnPeerManager, DataColumnReqResp {
  private final DataColumnPeerManagerStub dataColumnPeerManagerStub =
      new DataColumnPeerManagerStub();

  private final Map<UInt256, TestPeer> connectedPeers = new HashMap<>();

  public void connectPeer(final TestPeer peer) {
    dataColumnPeerManagerStub.addNode(peer.getNodeId());
    connectedPeers.put(peer.getNodeId(), peer);
  }

  public void disconnectPeer(final TestPeer peer) {
    dataColumnPeerManagerStub.removeNode(peer.getNodeId());
    peer.onDisconnect();
    connectedPeers.remove(peer.getNodeId());
  }

  @Override
  public SafeFuture<DataColumnSidecar> requestDataColumnSidecar(
      final UInt256 nodeId, final DataColumnSlotAndIdentifier columnIdentifier) {
    TestPeer peer = connectedPeers.get(nodeId);
    if (peer == null) {
      return SafeFuture.failedFuture(new DasPeerDisconnectedException());
    } else {
      return peer.requestSidecar(columnIdentifier.toDataColumnIdentifier());
    }
  }

  @Override
  public void flush() {}

  @Override
  public int getCurrentRequestLimit(final UInt256 nodeId) {
    TestPeer peer = connectedPeers.get(nodeId);
    if (peer == null) {
      return 0;
    } else {
      return peer.getCurrentRequestLimit();
    }
  }

  @Override
  public void addPeerListener(final PeerListener listener) {
    dataColumnPeerManagerStub.addPeerListener(listener);
  }

  @Override
  public void banNode(final UInt256 node) {
    dataColumnPeerManagerStub.banNode(node);
  }
}
