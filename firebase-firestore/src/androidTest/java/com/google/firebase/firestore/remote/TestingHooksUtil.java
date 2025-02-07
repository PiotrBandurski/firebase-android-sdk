// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.firestore.remote;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;

/** Convenience functions for accessing the testing hooks from {@link TestingHooks}. */
public final class TestingHooksUtil {

  /** Private constructor to prevent instantiation. */
  private TestingHooksUtil() {}

  /**
   * Captures all existence filter mismatches in the Watch 'Listen' stream that occur during the
   * execution of the given callback.
   *
   * @param callback The callback to invoke; during the invocation of this callback all existence
   *     filter mismatches will be captured.
   * @return the captured existence filter mismatches.
   */
  public static ArrayList<ExistenceFilterMismatchInfo> captureExistenceFilterMismatches(
      Runnable callback) {
    if (callback == null) {
      throw new NullPointerException("the given callback must not be null");
    }

    ArrayList<ExistenceFilterMismatchInfo> existenceFilterMismatches = new ArrayList<>();

    ListenerRegistration listenerRegistration =
        TestingHooks.getInstance()
            .addExistenceFilterMismatchListener(
                info -> {
                  synchronized (existenceFilterMismatches) {
                    existenceFilterMismatches.add(new ExistenceFilterMismatchInfo(info));
                  }
                });

    try {
      callback.run();
    } finally {
      listenerRegistration.remove();
    }

    // Return a *copy* of the `existenceFilterMismatches` list because, as documented in
    // addExistenceFilterMismatchListener(), it could *still* get notifications after it is
    // unregistered and that would be a race condition with the caller accessing the list.
    synchronized (existenceFilterMismatches) {
      return new ArrayList<>(existenceFilterMismatches);
    }
  }

  /** @see TestingHooks.ExistenceFilterMismatchInfo */
  public static final class ExistenceFilterMismatchInfo {

    private final TestingHooks.ExistenceFilterMismatchInfo info;

    ExistenceFilterMismatchInfo(@NonNull TestingHooks.ExistenceFilterMismatchInfo info) {
      this.info = info;
    }

    public int localCacheCount() {
      return info.localCacheCount();
    }

    public int existenceFilterCount() {
      return info.existenceFilterCount();
    }
  }
}
