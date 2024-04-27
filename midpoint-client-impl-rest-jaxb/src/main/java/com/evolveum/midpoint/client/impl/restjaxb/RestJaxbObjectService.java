/*
 * Copyright (c) 2017-2020 Evolveum
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
package com.evolveum.midpoint.client.impl.restjaxb;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author semancik
 *
 */
public class RestJaxbObjectService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectService<O> {

    public RestJaxbObjectService(final RestJaxbService service, final Class<O> type, final String oid) {
        super(service, type, oid);
	}

    @Deprecated
    @Override
	public O get() throws ObjectNotFoundException {
		return get(null);
	}

    @Deprecated
    @Override
    public O get(List<String> options) throws ObjectNotFoundException {
		return get(options, null, null);
	}

    @Deprecated
    @Override
    public O get(List<String> options, List<String> include, List<String> exclude) throws ObjectNotFoundException {
        return getService().getObject(getType(), getOid(), options, include, exclude);
	}

	@Override
	public void delete() throws ObjectNotFoundException {
		 getService().deleteObject(getType(), getOid());
	}

	@Override
	public ObjectModifyService<O> modify() throws ObjectNotFoundException {
		return new RestJaxbObjectModifyService<>(getService(), getType(), getOid());
	}

    @Override
    public ObjectReadService<O> read() {
        return new RestJaxbObjectReadService<>(getService(), getType(), getOid());
    }

    public ObjectRemoveService<O> remove() {
        return new RestJaxbObjectRemoveService<>(getService(), getType(), getOid());
    }

    @Override
    public ObjectReplaceService<O> replace(O object) {
        return new RestJaxbObjectReplaceService<>(getService(), getType(), getOid(), object);
    }
}
