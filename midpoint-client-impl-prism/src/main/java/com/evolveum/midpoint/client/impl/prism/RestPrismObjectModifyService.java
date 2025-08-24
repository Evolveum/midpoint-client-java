/*
 * Copyright (c) 2017-2025 Evolveum
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.client.api.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

/**
 * @author Z14tk0
 */
public class RestPrismObjectModifyService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectModifyService<O> {

    private List<ItemDeltaType> modifications;
    private List<String> options;

    @Override
    public ExecuteOptionSupport.WithPost<ObjectReference<O>> options() {
        return new ExecuteOptionsBuilder.WithPost<ObjectReference<O>>() {
            @Override
            public TaskFuture<ObjectReference<O>> apost() throws CommonException {
                setOptions(this.optionsAsStringList());
                return RestPrismObjectModifyService.this.apost();
            }
        };
    }

    public RestPrismObjectModifyService(RestPrismService service, ObjectTypes type, String oid) {
        super(service, type.getClassDefinition(), oid);
        this.modifications = new ArrayList<>();
    }

    public RestPrismObjectModifyService<O> setOptions(List<String> options) {
        this.options = options;
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> add(String path, Object value){
        addModification(path, value, ModificationTypeType.ADD);
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> add(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.ADD);
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> replace(String path, Object value){
        addModification(path, value, ModificationTypeType.REPLACE);
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> replace(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.REPLACE);
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> delete(String path, Object value){
        addModification(path, value, ModificationTypeType.DELETE);
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> delete(Map<String, Object> modifications){
        addModifications(modifications, ModificationTypeType.DELETE);
        return this;
    }

    @Override
    public RestPrismObjectModifyService<O> setModifications(InputStream inputStream) throws SchemaException {
        this.modifications.addAll(((ObjectModificationType) ((RestPrismService) getService()).parseObjectModification(inputStream)).getItemDelta());
        return this;
    }

    private void addModification(String path, Object value, ModificationTypeType modificationType){
        modifications.add(RestPrismUtils.buildItemDelta(modificationType, path, value));
    }

    private void addModifications(Map<String, Object> modifications, ModificationTypeType modificationType){
        modifications.forEach((path, value) ->
        addModification(path, value, modificationType));
    }

    @Override
    public TaskFuture<ObjectReference<O>> apost() throws SchemaException, ObjectAlreadyExistsException, ObjectNotFoundException {

        String oidRes = ((RestPrismService) getService()).modifyObject(ObjectTypes.getObjectType(getType()), RestPrismUtils.buildModifyObject(modifications), options, getOid());

        RestPrismObjectReference<O> ref = new RestPrismObjectReference<>((RestPrismService) getService(), oidRes, getType());
        return new RestPrismCompletedFuture<>(ref);
    }

}
