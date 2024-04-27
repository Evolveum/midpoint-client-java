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

import java.util.ArrayList;
import java.util.List;

public abstract class ExecuteOptionsBuilder<S extends ExecuteOptionSupport<S>> implements ExecuteOptionSupport<S> {

    List<ExecuteOption> options = new ArrayList<>();

    @Override
    public S option(ExecuteOption option) {
        if (option != null) {
            options.add(option);
        }
        return self();
    }

    @Override
    public List<String> getOptions() {

        return null;
    }

    public List<String> optionsAsStringList() {
        List<String> ret = new ArrayList<>(options.size());
        for (var opt : options) {
            ret.add(opt.toString());
        }
        return ret;
    }

    public static abstract class WithPost<P> extends ExecuteOptionsBuilder<ExecuteOptionSupport.WithPost<P>> implements ExecuteOptionSupport.WithPost<P> {

    }

    public static abstract class WithDelete<P> extends  ExecuteOptionsBuilder<ExecuteOptionSupport.WithDelete<P>> implements ExecuteOptionSupport.WithDelete<P> {

    }

    public static abstract class WithPut<P> extends ExecuteOptionsBuilder<ExecuteOptionSupport.WithPut<P>> implements ExecuteOptionSupport.WithPut<P> {

    }
}
