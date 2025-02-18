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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evolveum.midpoint.client.api.*;

import com.evolveum.midpoint.client.api.exception.*;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.evolveum.midpoint.client.api.query.FilterEntryOrEmpty;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;

/**
 * @author semancik
 * @author katkav
 *
 */
public class RestJaxbSearchService<O extends ObjectType> extends AbstractObjectTypeWebResource<O> implements SearchService<O> {

	private QueryType query;
    private GetOptionsBuilder.WithGet<SearchResult<O>> options = new GetOptionsBuilder.WithGet<SearchResult<O>>() {
        @Override
        public SearchResult<O> get() throws ObjectNotFoundException, SchemaException, AuthenticationException, AuthorizationException, InternalServerErrorException {
            return RestJaxbSearchService.this.get();
        }
    };

	public RestJaxbSearchService(final RestJaxbService service, final Class<O> type) {
		this(service, type, null);
	}

	public RestJaxbSearchService(final RestJaxbService service, final Class<O> type, final QueryType query) {
		super(service, type);
		this.query = query;
	}

		@Override
	public SearchResult<O> get(List<String> options) throws ObjectNotFoundException {
        if (options != null) {
            for (var opt : options) {
                options().option(GetOption.from(opt));
            }
        }
	    return get(null);
	}

    @Override
    public SearchResult<O> get() throws ObjectNotFoundException {
        String path = "/" + Types.findType(getType()).getRestPath() + "/search";
        Map<String, List<String>> queryParams = new HashMap<>();

        var stringOptions = options.optionsAsStringList();

        if (!stringOptions.isEmpty()) {
            queryParams.put("options", stringOptions);
        }
        if (!options.getInclude().isEmpty()) {
            queryParams.put("include", options.getInclude());
        }
        if (!options.getExclude().isEmpty()) {
            queryParams.put("exclude", options.getExclude());
        }
        if (options.getReturnTotalCount()) {
            queryParams.put("returnTotalCount", Collections.singletonList(Boolean.toString(options.getReturnTotalCount())));
        }
        Response response = getService().post(path, query, queryParams);

        if (Status.OK.getStatusCode() == response.getStatus()) {
            return new JaxbSearchResult<>(getSearchResultList(response), response.getHeaders());
        }

        if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new ObjectNotFoundException("Cannot search objects. No such service");
        }
        return null;
    }

    @Override
    public GetOptionSupport.WithGet<SearchResult<O>> options() {
        return options;
    }

    @Override
	public FilterEntryOrEmpty<O> queryFor(Class<O> type) {
		return new FilterBuilder<O>(getService(), getType());
	}

	@SuppressWarnings("unchecked")
	private List<O> getSearchResultList(Response response) {
        if (response.hasEntity()) {

            ObjectListType resultList = response.readEntity(ObjectListType.class);
            return (List<O>) resultList.getObject();
        } else {
            return null;
        }

    }
}
