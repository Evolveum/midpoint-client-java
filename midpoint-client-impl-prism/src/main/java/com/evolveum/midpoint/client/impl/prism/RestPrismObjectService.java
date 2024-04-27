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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.*;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;

public class RestPrismObjectService<O extends ObjectType> extends CommonPrismService implements ObjectService<O> {

    private String oid;

    public RestPrismObjectService(RestPrismService service, ObjectTypes type, String oid) {
        super(service, type);
        this.oid = oid;
    }


    @Override
    public O get(List<String> options) throws ObjectNotFoundException, AuthenticationException {
        throw new UnsupportedOperationException("Not impelemted yet");
    }

    @Override
    public O get(List<String> options, List<String> include, List<String> exclude) throws ObjectNotFoundException, AuthenticationException {
        throw new UnsupportedOperationException("Not impelemted yet");
    }

    @Override
    public ObjectModifyService<O> modify() throws ObjectNotFoundException, AuthenticationException {
        throw new UnsupportedOperationException("Not impelemted yet");
    }

    @Override
    public ObjectReplaceService<O> replace(O object) throws AuthenticationException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ObjectReadService<O> read() {
        return new ObjectReadService<O>() {
            @Override
            public GetOptionSupport.WithGet<O> options() {
                return null;
            }

            @Override
            public O get() throws ObjectNotFoundException, SchemaException, AuthenticationException, AuthorizationException, InternalServerErrorException {
                return getService().getObject(getType(), oid);
            }
        };
    }

    @Override
    public ObjectRemoveService<O> remove() {
        return new ObjectRemoveService<O>() {
            @Override
            public ExecuteOptionSupport.WithDelete<O> options() {
                return null;
            }

            @Override
            public void delete() throws ObjectNotFoundException, AuthenticationException, InternalServerErrorException {
                try {
                    getService().deleteObject(getType(), getOid());
                } catch (SchemaException e) { // TODO add to methid signature?
                    throw new SystemException(e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public void delete() throws ObjectNotFoundException, AuthenticationException {
        try {
            getService().deleteObject(getType(), getOid());
        } catch (SchemaException e) { // TODO add to methid signature?
            throw new SystemException(e.getMessage(), e);
        }
    }

    @Override
    public O get() throws ObjectNotFoundException, AuthenticationException, SchemaException {
        O response = getService().getObject(getType(), oid);

        if (response == null) {
            throw new ObjectNotFoundException("null returned");
        }

        return response;
    }

    String getOid() {
        return oid;
    }

}


