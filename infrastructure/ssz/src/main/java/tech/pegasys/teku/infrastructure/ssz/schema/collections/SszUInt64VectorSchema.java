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

package tech.pegasys.teku.infrastructure.ssz.schema.collections;

import tech.pegasys.teku.infrastructure.ssz.collections.SszUInt64Vector;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.impl.SszUInt64VectorSchemaImpl;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public interface SszUInt64VectorSchema<SszVectorT extends SszUInt64Vector>
    extends SszPrimitiveVectorSchema<UInt64, SszUInt64, SszVectorT> {

  static SszUInt64VectorSchema<SszUInt64Vector> create(final long maxLength) {
    return new SszUInt64VectorSchemaImpl<>(maxLength);
  }
}
