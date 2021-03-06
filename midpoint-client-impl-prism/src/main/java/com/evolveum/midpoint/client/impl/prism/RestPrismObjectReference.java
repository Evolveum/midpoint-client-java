/*
 * Copyright (c) 2021 Evolveum
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
package com.evolveum.midpoint.client.impl.prism;

import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class RestPrismObjectReference<O extends ObjectType> implements ObjectReference<O> {

    private String oid;
    private Class<O> type;

    public RestPrismObjectReference(String oid, Class type) {
        this.oid = oid;
        this.type = type;
    }

    @Override
    public String getOid() {
        return oid;
    }

    @Override
    public Class<O> getType() {
        return type;
    }

    @Override
    public O getObject() throws ObjectNotFoundException {
        return null;
    }

    @Override
    public boolean containsObject() {
        return false;
    }

    @Override
    public O get() throws ObjectNotFoundException {
        return null;
    }
}
