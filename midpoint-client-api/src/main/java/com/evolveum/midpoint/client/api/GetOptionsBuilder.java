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

public class GetOptionsBuilder<F extends GetOptionSupport<F>> implements GetOptionSupport<F> {

    List<GetOption> options = new ArrayList<>();
    List<String> include = new ArrayList<>();
    List<String> exclude = new ArrayList<>();
    Boolean returnTotalCount = false;

    @Override
    public F include(String path) {
        include.add(path);
        return self();
    }

    @Override
    public F exclude(String path) {
        exclude.add(path);
        return self();
    }

    @Override
    public F option(GetOption option) {
        if (option != null) {
            options.add(option);
        }
        return self();
    }

    public F removeOption(GetOption option) {
        if (option != null) {
            options.remove(option);
        }
        return self();
    }

    @Override
    public F returnTotalCount() {
        this.returnTotalCount = true;
        return self();
    }

    public List<String> getExclude() {
        return exclude;
    }

    public List<String> getInclude() {
        return include;
    }

    public Boolean getReturnTotalCount() {
        return returnTotalCount;
    }

    public List<String> optionsAsStringList() {
        List<String> ret = new ArrayList<>(options.size());
        for (var opt : options) {
            ret.add(opt.toString());
        }
        return ret;
    }

    public abstract static class WithGet<O> extends GetOptionsBuilder<GetOptionSupport.WithGet<O>> implements GetOptionSupport.WithGet<O> {

    }

    public abstract static class WithPost<O> extends GetOptionsBuilder<GetOptionSupport.WithPost<O>> implements GetOptionSupport.WithPost<O> {

    }
}
