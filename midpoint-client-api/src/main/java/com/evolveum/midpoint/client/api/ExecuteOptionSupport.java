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

import com.evolveum.midpoint.client.api.verb.Delete;
import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.client.api.verb.Put;

import java.util.List;

import static com.evolveum.midpoint.client.api.ExecuteOption.*;
public interface ExecuteOptionSupport<S extends ExecuteOptionSupport> {


     S option(ExecuteOption option);

     default S option(String option) {
        option(ExecuteOption.from(option));
        return self();
    }

    default S mergeOptions(List<String> options) {
        if (options != null) {
            for (var opt: options) {
                option(opt);
            }
        }
        return self();
    }

    default S self() {
        return (S) this;
    }

    List<String> getOptions();

    interface WithPost<P> extends ExecuteOptionSupport<WithPost<P>>, Post<P> {

    }

    interface  WithDelete<P> extends ExecuteOptionSupport<WithDelete<P>>, Delete<P> {

    }

    interface WithPut<P> extends ExecuteOptionSupport<WithPut<P>>, Put<P> {

    }

    default S raw() {
        return option(RAW);
    }

    default S executeImmediatelyAfterApproval() {
        return option(EXECUTE_IMMEDIATELY_AFTER_APPROVAL);
    }

    default S force() {
        return option(FORCE);
    }

    default S pushChanges() {
        return option(PUSH_CHANGES);
    }

    default S noCrypt() {
        return option(NO_CRYPT);
    }

    default S overwrite() {
        return option(OVERWRITE);
    }

    default S reconcile() {
        return option(RECONCILE);
    }

    default S isImport() {
        return option(IS_IMPORT);
    }

    default S limitPropagation() {
        return option(LIMIT_PROPAGATION);
    }

    default S reevaluateSearchFilters() {
        return option(REEVALUATE_SEARCH_FILTERS);
    }


}
