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

import java.util.ArrayList;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.evolveum.midpoint.client.api.*;

import com.evolveum.midpoint.client.api.exception.*;

import jakarta.ws.rs.core.Response;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

/**
 * @author jakmor
 */
public class RestJaxbObjectModifyService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectModifyService<O> {

    private List<ItemDeltaType> modifications;

    private ExecuteOptionsBuilder.WithPost<ObjectReference<O>> options = new ExecuteOptionsBuilder.WithPost<ObjectReference<O>>() {

        @Override
        public TaskFuture<ObjectReference<O>> apost() throws CommonException {
            return RestJaxbObjectModifyService.this.apost();
        }
    };

    public RestJaxbObjectModifyService(RestJaxbService service, Class<O> type, String oid) {
        super(service, type, oid);
        modifications = new ArrayList<>();
    }

    @Override
    public RestJaxbObjectModifyService<O> add(String path, Object value){
        addModification(path, value, ModificationTypeType.ADD);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> add(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.ADD);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> replace(String path, Object value){
        addModification(path, value, ModificationTypeType.REPLACE);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> replace(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.REPLACE);
        return this;
    }


    @Override
    public RestJaxbObjectModifyService<O> delete(String path, Object value){
        addModification(path, value, ModificationTypeType.DELETE);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> delete(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.DELETE);
        return this;
    }

    @Override
    public RestJaxbObjectModifyService<O> setModifications(InputStream inputStream) throws SchemaException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void addModification(String path, Object value, ModificationTypeType modificationType){
        modifications.add(RestUtil.buildItemDelta(modificationType, path, value));
    }

    private void addModifications(Map<String, Object> modifications,  ModificationTypeType modificationType){
        modifications.forEach((path, value) ->
        addModification(path, value, modificationType));
    }

    @Override
    public TaskFuture<ObjectReference<O>> apost() throws ObjectNotFoundException {

        Map<String, List<String>> queryParams = null;
        var stringOptions = options.optionsAsStringList();
        if (!stringOptions.isEmpty()) {
            queryParams = Map.of("options", stringOptions);
        }

        String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), getOid());
        Response response = getService().post(restPath, RestUtil.buildModifyObject(modifications), queryParams);
        switch (response.getStatus()) {
            case 204:
            case 240: // handled error
                RestJaxbObjectReference<O> ref = new RestJaxbObjectReference<>(getService(), getType(), getOid());
                return new RestJaxbCompletedFuture<>(ref);
            case 250:
                throw new PartialErrorException(response.getStatusInfo().getReasonPhrase());
            default:
                throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
        }
    }

    @Override
    public ExecuteOptionSupport.WithPost<ObjectReference<O>> options() {
        return options;
    }
}
