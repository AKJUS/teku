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

package tech.pegasys.teku.kzg;

import static com.google.common.base.Preconditions.checkArgument;
import static ethereum.ckzg4844.CKZG4844JNI.BYTES_PER_PROOF;

import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes48;
import org.apache.tuweni.ssz.SSZ;

public final class KZGProof {
  public static final KZGProof ZERO = fromArray(new byte[BYTES_PER_PROOF]);

  public static KZGProof fromHexString(final String hexString) {
    return KZGProof.fromBytesCompressed(Bytes48.fromHexString(hexString));
  }

  public static KZGProof fromSSZBytes(final Bytes bytes) {
    checkArgument(
        bytes.size() == BYTES_PER_PROOF,
        "Expected " + BYTES_PER_PROOF + " bytes but received %s.",
        bytes.size());
    return SSZ.decode(
        bytes, reader -> new KZGProof(Bytes48.wrap(reader.readFixedBytes(BYTES_PER_PROOF))));
  }

  public static KZGProof fromBytesCompressed(final Bytes48 bytes) throws IllegalArgumentException {
    return new KZGProof(bytes);
  }

  public static KZGProof fromArray(final byte[] bytes) {
    return fromBytesCompressed(Bytes48.wrap(bytes));
  }

  static List<KZGProof> splitBytes(final Bytes bytes) {
    return CKZG4844Utils.bytesChunked(bytes, BYTES_PER_PROOF).stream()
        .map(b -> new KZGProof(Bytes48.wrap(b)))
        .toList();
  }

  private final Bytes48 bytesCompressed;

  public KZGProof(final Bytes48 bytesCompressed) {
    this.bytesCompressed = bytesCompressed;
  }

  /**
   * Returns the SSZ serialization of the <em>compressed</em> form of the proof
   *
   * @return the serialization of the compressed form of the proof.
   */
  public Bytes toSSZBytes() {
    return SSZ.encode(writer -> writer.writeFixedBytes(getBytesCompressed()));
  }

  public Bytes48 getBytesCompressed() {
    return bytesCompressed;
  }

  public byte[] toArrayUnsafe() {
    return bytesCompressed.toArrayUnsafe();
  }

  public String toAbbreviatedString() {
    return getBytesCompressed().toUnprefixedHexString().substring(0, 7);
  }

  public String toHexString() {
    return getBytesCompressed().toHexString();
  }

  @Override
  public String toString() {
    return getBytesCompressed().toString();
  }

  @Override
  public boolean equals(final Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (obj instanceof final KZGProof other) {
      return Objects.equals(this.getBytesCompressed(), other.getBytesCompressed());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(getBytesCompressed());
  }
}
