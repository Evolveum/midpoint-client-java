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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.query.AtomicFilterExit;
import com.evolveum.midpoint.client.api.query.ConditionEntry;
import com.evolveum.midpoint.client.api.query.FilterEntry;
import com.evolveum.midpoint.client.api.query.FilterExit;
import com.evolveum.midpoint.client.api.query.MatchingRuleEntry;
import com.evolveum.midpoint.client.api.query.RightHandItemEntry;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;


/**
 *
 * @author katkav
 *
 */
public class RestJaxbQueryBuilder<O extends ObjectType> implements ConditionEntry<O>, MatchingRuleEntry<O>, RightHandItemEntry<O> {

	private ItemPathType itemPath;
	private RestJaxbQueryBuilder<O> originalFilter;
	private Element filterClause;

	private RestJaxbService queryForService;
	private Class<O> type;

//	private QueryType query;

	private FilterBuilder<O> owner;

	private RestJaxbQueryBuilder(RestJaxbQueryBuilder<O> originalFilter, ItemPathType itemPath, FilterBuilder<O> owner) {
		this(originalFilter.queryForService, originalFilter.type);
		this.itemPath = itemPath;
		this.owner = owner;
	}

	// TODO reconsider the visibility
	RestJaxbQueryBuilder(RestJaxbQueryBuilder<O> originalFilter, Element filterClause, FilterBuilder<O> owner) {
		this(originalFilter.queryForService, originalFilter.type);
		this.originalFilter = originalFilter;
		this.filterClause = filterClause;
		this.owner = owner;
	}

//	public RestJaxbQueryBuilder(RestJaxbService searchService, Class<O> type, QueryType query) {
//		this(searchService, type);
//		this.query = query;
////		this.owner = owner;
//	}

	private RestJaxbQueryBuilder(RestJaxbService searchService, Class<O> type) {
		this.queryForService = searchService;
		this.type = type;

	}

	private RestJaxbQueryBuilder(RestJaxbService searchService, Class<O> type, FilterBuilder<O> owner) {
		this.queryForService = searchService;
		this.type = type;
		this.owner = owner;
	}


	public static <O extends ObjectType>  RestJaxbQueryBuilder<O> create(RestJaxbService serachService, Class<O> type, FilterBuilder<O> owner, ItemPathType itemPath){
		RestJaxbQueryBuilder<O> restJaxbBuilder = new RestJaxbQueryBuilder<>(serachService, type, owner);
		restJaxbBuilder.itemPath = itemPath;
		return restJaxbBuilder;
	}

	@Override
	public SearchService<O> build() {
		return finish().build();
//		SearchFilterType filter = finish().buildFilter();
//		QueryType query = new QueryType();
//		query.setFilter(filter);
//		return new RestJaxbSearchService<O>(queryForService, type, query);
	}


	@Override
	public MatchingRuleEntry<O> eq(Object... values) {
		Element equal = queryForService.getDomSerializer().createEqualFilter(itemPath, Arrays.asList(values));
		return new RestJaxbQueryBuilder<O>(this, equal, owner);
	}

	@Override
	public AtomicFilterExit<O> item(ItemPathType itemPath) {
		return new RestJaxbQueryBuilder<O>(this, itemPath, owner);
	}

	@Override
	public AtomicFilterExit<O> item(QName... qnames) {
		return new RestJaxbQueryBuilder<>(this, queryForService.util().createItemPathType(qnames), owner);
	}


	@Override
	public MatchingRuleEntry<O> eq() {
		Element equal = queryForService.getDomSerializer().createEqualFilter(itemPath, null);
		return new RestJaxbQueryBuilder<O>(this, equal, owner);
	}

	@Override
	public MatchingRuleEntry<O> eqPoly(String orig, String norm) {
		Element equal = queryForService.getDomSerializer().createEqualPolyFilter(itemPath, orig, norm);
		return new RestJaxbQueryBuilder<O>(this, equal, owner);
	}

	@Override
	public MatchingRuleEntry<O> eqPoly(String orig) {
		Element equal = queryForService.getDomSerializer().createEqualPolyFilter(itemPath, orig, null);
		return new RestJaxbQueryBuilder<O>(this, equal, owner);
	}

	@Override
	public MatchingRuleEntry<O> gt(Object value) {
		Element greater = queryForService.getDomSerializer().createGreaterFilter(itemPath, value);
		return new RestJaxbQueryBuilder<O>(this, greater, owner);
	}

	@Override
	public AtomicFilterExit<O> gt() {
		Element greater = queryForService.getDomSerializer().createGreaterFilter(itemPath, null);
		return new RestJaxbQueryBuilder<O>(this, greater, owner);
	}

	@Override
	public MatchingRuleEntry<O> ge(Object value) {
		// TODO Auto-generated method stubo
		return null;
	}

	@Override
	public AtomicFilterExit<O> ge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> lt(Object value) {
		Element less = queryForService.getDomSerializer().createLessFilter(itemPath, value);
		return new RestJaxbQueryBuilder<O>(this, less, owner);
	}

	@Override
	public AtomicFilterExit<O> lt() {
		Element less = queryForService.getDomSerializer().createLessFilter(itemPath, null);
		return new RestJaxbQueryBuilder<O>(this, less, owner);
	}

	@Override
	public MatchingRuleEntry<O> le(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> le() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> startsWith(Object value) {
		Element substring = queryForService.getDomSerializer().createSubstringFilter(itemPath, value, true, false);
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public MatchingRuleEntry<O> startsWithPoly(String orig, String norm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> startsWithPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> endsWith(Object value) {
		Element substring = queryForService.getDomSerializer().createSubstringFilter(itemPath, value, false, true);
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public MatchingRuleEntry<O> endsWithPoly(String orig, String norm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> endsWithPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> contains(Object value) {
		Element substring = queryForService.getDomSerializer().createSubstringFilter(itemPath, value, false, false);
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public MatchingRuleEntry<O> containsPoly(String orig, String norm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatchingRuleEntry<O> containsPoly(String orig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> ref(QName relation) {
		ObjectReferenceType refType = new ObjectReferenceType();
		refType.setRelation(relation);
		Element substring = queryForService.getDomSerializer().createRefFilter(itemPath, Arrays.asList(refType));
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public AtomicFilterExit<O> ref(ObjectReferenceType... value) {
		Element substring = queryForService.getDomSerializer().createRefFilter(itemPath, Arrays.asList(value));
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public AtomicFilterExit<O> ref(Collection<ObjectReferenceType> values) {
		Element substring = queryForService.getDomSerializer().createRefFilter(itemPath, values);
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public AtomicFilterExit<O> ref(String... oids) {
		List<ObjectReferenceType> refTypes = new ArrayList<>();
		for (String oid : oids) {
			ObjectReferenceType refType = new ObjectReferenceType();
			refType.setOid(oid);
			refTypes.add(refType);
		}
		Element substring = queryForService.getDomSerializer().createRefFilter(itemPath, refTypes);
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public AtomicFilterExit<O> ref(String oid, QName targetTypeName) {
		ObjectReferenceType refType = new ObjectReferenceType();
		refType.setOid(oid);
		refType.setType(targetTypeName);
		Element substring = queryForService.getDomSerializer().createRefFilter(itemPath, Arrays.asList(refType));
		return new RestJaxbQueryBuilder<O>(this, substring, owner);
	}

	@Override
	public AtomicFilterExit<O> isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterEntry<O> and() {
		return finish().and();
	}

	@Override
	public FilterEntry<O> or() {
		return finish().or();
	}

	private FilterBuilder<O> finish() {
		if (filterClause == null) {
			throw new IllegalStateException("No filter created yet.");
		}
		return owner.addSubfilter(filterClause, false);
	}

//	@Override
//	public QueryBuilder<O> finishQuery() {
//		return finish().finishQuery();
//		QueryType query = new QueryType();
//		query.setFilter(buildFilter());;
//
//		return new RestJaxbQueryBuilder<>(queryForService, type, query, owner);
//	}

	public AtomicFilterExit<O> appendMatchingRuleElement(Element filterClause, DomSerializer.MatchingRuleType matchingRuleType){
		Element appendedFilter = queryForService.getDomSerializer().appendMatchingRuleElement(filterClause, matchingRuleType);
		return new RestJaxbQueryBuilder<O>(this, appendedFilter, owner);
	}

	@Override
	public AtomicFilterExit<O> matchingOrig() {
		return appendMatchingRuleElement(filterClause, DomSerializer.MatchingRuleType.POLY_STRING_ORIG);
	}

	@Override
	public AtomicFilterExit<O> matchingNorm() {
		return appendMatchingRuleElement(filterClause, DomSerializer.MatchingRuleType.POLY_STRING_NORM);
	}

	@Override
	public AtomicFilterExit<O> matchingStrict() {
		return appendMatchingRuleElement(filterClause, DomSerializer.MatchingRuleType.POLY_STRING_STRICT);
	}

	@Override
	public AtomicFilterExit<O> matchingCaseIgnore() {
		return appendMatchingRuleElement(filterClause, DomSerializer.MatchingRuleType.STRING_IGNORE_CASE);
	}

	@Override
	public AtomicFilterExit<O> matching(QName matchingRuleName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AtomicFilterExit<O> endBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterExit<O> asc(QName... names) {
		return finish().asc(names);
	}



	@Override
	public FilterExit<O> asc(ItemPathType path) {
		return finish().asc(path);
	}

	@Override
	public FilterExit<O> desc(QName... names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterExit<O> desc(ItemPathType path) {
		return finish().desc(path);
	}

	@Override
	public FilterExit<O> group(QName... names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterExit<O> group(ItemPathType path) {
		return finish().group(path);
	}

	@Override
	public FilterExit<O> offset(Integer n) {
		return finish().offset(n);
	}

	@Override
	public FilterExit<O> maxSize(Integer n) {
		return finish().maxSize(n);
	}


//	@Override
//	public PagingRuleBuilder<O> paging()
//	{
//		PagingType pagingType = new PagingType();
//
//		//TODO: Temporary provision to prevent null pointer exception if paging is called before finishQuery()
//		if(query == null) {
//			query = new QueryType();
//		}
//		query.setPaging(pagingType);
//
//		return new RestJaxbQueryBuilder<O>(queryForService, type, query);
//	}
//
//	@Override
//	public PagingRuleBuilder<O> orderBy(ItemPathType itemPath)
//	{
//		query.getPaging().setOrderBy(itemPath);
//		return new RestJaxbQueryBuilder<O>(queryForService, type, query);
//	}
//
//	@Override
//	public PagingRuleBuilder<O> groupBy(ItemPathType itemPath)
//	{
//		query.getPaging().setGroupBy(itemPath);
//		return new RestJaxbQueryBuilder<O>(queryForService, type, query);
//	}
//
//	@Override
//	public PagingRuleBuilder<O> offSet(Integer offsetAmount)
//	{
//		query.getPaging().setOffset(offsetAmount);
//		return new RestJaxbQueryBuilder<O>(queryForService, type, query);
//	}
//
//	@Override
//	public PagingRuleBuilder<O> maxSize(Integer maxSize)
//	{
//		query.getPaging().setMaxSize(maxSize);
//		return new RestJaxbQueryBuilder<O>(queryForService, type, query);
//	}
//
//	@Override
//	public QueryBuilder<O> finishPaging()
//	{
//		return new RestJaxbQueryBuilder<O>(queryForService, type, query);
//	}

}
