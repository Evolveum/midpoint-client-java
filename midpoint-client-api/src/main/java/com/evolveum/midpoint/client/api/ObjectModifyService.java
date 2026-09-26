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
package com.evolveum.midpoint.client.api;

import java.io.InputStream;
import java.util.Map;

import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * @author jakmor
 */
public interface ObjectModifyService <O extends ObjectType> extends Post<ObjectReference<O>> {
    ObjectModifyService<O> add(String path, Object value);
    ObjectModifyService<O> add(Map<String, Object> modifications);
    ObjectModifyService<O> replace(String path, Object value);
    ObjectModifyService<O> replace(Map<String, Object> modifications);
    ObjectModifyService<O> delete(String path, Object value);
    ObjectModifyService<O> delete(Map<String, Object> modifications);
    ObjectModifyService<O> setModifications(InputStream inputStream) throws SchemaException;

    ExecuteOptionSupport.WithPost<ObjectReference<O>> options();

}
