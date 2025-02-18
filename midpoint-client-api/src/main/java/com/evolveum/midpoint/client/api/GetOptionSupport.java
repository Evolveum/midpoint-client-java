/*
 * Copyright (c) 2023 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.verb.Get;
import com.evolveum.midpoint.client.api.verb.Post;

public interface GetOptionSupport<S extends GetOptionSupport<S>> {

    S option(GetOption option);

    S removeOption(GetOption option);

    default S raw() {
        option(GetOption.RAW);
        return self();
    }

    default S noFetch() {
        option(GetOption.NO_FETCH);
        return self();
    }

    default S noDiscovery() {
        option(GetOption.NO_DISCOVERY);
        return self();
    }

    default S resolveNames() {
        option(GetOption.RESOLVE_NAMES);
        return self();
    }

    S include(String path);

    S exclude(String path);

    S returnTotalCount();

    default S distinct() {
        option(GetOption.DISTINCT);
        return self();
    }

    default S self() {
        return (S) this;
    }

    interface WithGet<T> extends GetOptionSupport<WithGet<T>>, Get<T> {

    }
    interface WithPost<T> extends GetOptionSupport<WithPost<T>>, Post<T> {

    }
}
