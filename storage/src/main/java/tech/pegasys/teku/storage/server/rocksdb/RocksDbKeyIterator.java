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

package tech.pegasys.teku.storage.server.rocksdb;

import com.google.errorprone.annotations.MustBeClosed;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import tech.pegasys.teku.storage.server.ShuttingDownException;
import tech.pegasys.teku.storage.server.kvstore.schema.KvStoreColumn;

class RocksDbKeyIterator<TKey, TValue> implements Iterator<byte[]>, AutoCloseable {
  private static final Logger LOG = LogManager.getLogger();

  private final KvStoreColumn<TKey, TValue> column;
  private final RocksIterator rocksIterator;
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final Predicate<TKey> continueTest;
  private final Supplier<Boolean> isDatabaseClosed;

  private RocksDbKeyIterator(
      final KvStoreColumn<TKey, TValue> column,
      final RocksIterator rocksIterator,
      final Predicate<TKey> continueTest,
      final Supplier<Boolean> isDatabaseClosed) {
    this.column = column;
    this.rocksIterator = rocksIterator;
    this.continueTest = continueTest;
    this.isDatabaseClosed = isDatabaseClosed;
  }

  @MustBeClosed
  public static <K, V> RocksDbKeyIterator<K, V> create(
      final KvStoreColumn<K, V> column,
      final RocksIterator rocksIt,
      final Predicate<K> continueTest,
      final Supplier<Boolean> isDatabaseClosed) {
    return new RocksDbKeyIterator<>(column, rocksIt, continueTest, isDatabaseClosed);
  }

  @Override
  public boolean hasNext() {
    assertOpen();
    return rocksIterator.isValid()
        && continueTest.test(column.getKeySerializer().deserialize(rocksIterator.key()));
  }

  @Override
  public byte[] next() {
    assertOpen();
    try {
      rocksIterator.status();
    } catch (final RocksDBException e) {
      LOG.error("RocksDbEntryIterator encountered a problem while iterating.", e);
    }
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    final byte[] entry = rocksIterator.key();
    rocksIterator.next();
    return entry;
  }

  @MustBeClosed
  public Stream<byte[]> toStream() {
    assertOpen();
    final Spliterator<byte[]> split =
        Spliterators.spliteratorUnknownSize(
            this,
            Spliterator.IMMUTABLE
                | Spliterator.DISTINCT
                | Spliterator.NONNULL
                | Spliterator.ORDERED
                | Spliterator.SORTED);

    return StreamSupport.stream(split, false).onClose(this::close);
  }

  private void assertOpen() {
    if (isDatabaseClosed.get()) {
      throw new ShuttingDownException();
    }
    if (closed.get()) {
      throw new IllegalStateException(
          "Attempt to update a closed " + this.getClass().getSimpleName());
    }
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      rocksIterator.close();
    }
  }
}
