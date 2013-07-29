/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.classes.resources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 24.07.13 12:12
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public enum Delegation {
  CALL_PARENT_FIRST {
    @Nullable
    @Override
    public <T, R> R apply(@NotNull final ValueAction<T, R> action, @NotNull final T t) {
      final R e = action.callParent(t);
      if (e != null) return e;
      return action.callSelf(t);
    }
  },

  CALL_SELF_FIRST {
    @Nullable
    @Override
    public <T, R> R apply(@NotNull final ValueAction<T, R> action, @NotNull final T t) {
      final R e = action.callSelf(t);
      if (e != null) return e;
      return action.callParent(t);
    }
  },
  ;

  @Nullable
  public abstract <T, R> R apply(@NotNull final ValueAction<T, R> action, @NotNull T t);

  public interface ValueAction<T, R> {
    @Nullable
    R callParent(@NotNull T r);

    @Nullable
    R callSelf(@NotNull T r);
  }


}
