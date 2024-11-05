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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.cxf.common.util.CollectionUtils;

import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 *
 * @author katkav
 *
 */
public class JaxbSearchResult<O extends ObjectType> implements SearchResult<O> {

	private List<O> list = null;
    private MultivaluedMap<String, Object> headers;

	public JaxbSearchResult() {
	}

	public JaxbSearchResult(List<O> list) {
		super();
		this.list = list;
	}

    public JaxbSearchResult(List<O> list, MultivaluedMap<String, Object> headers) {
        this.list = list;
        this.headers = headers;
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

	@Override
	public int size() {
		if (list == null) {
			return 0;
		}
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(list);
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<O> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public boolean add(O e) {
		return list.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends O> c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends O> c) {
		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public O get(int index) {
		return list.get(index);
	}

	@Override
	public O set(int index, O element) {
		return list.set(index, element);
	}

	@Override
	public void add(int index, O element) {
		list.add(index, element);
	}

	@Override
	public O remove(int index) {
		return list.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<O> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<O> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<O> subList(int fromIndex, int toIndex) {
		return subList(fromIndex, toIndex);
	}



}
