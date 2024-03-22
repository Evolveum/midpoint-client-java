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

import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.prism.xml.ns._public.query_3.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.query.AtomicFilterEntry;
import com.evolveum.midpoint.client.api.query.AtomicFilterExit;
import com.evolveum.midpoint.client.api.query.ConditionEntry;
import com.evolveum.midpoint.client.api.query.FilterEntry;
import com.evolveum.midpoint.client.api.query.FilterEntryOrEmpty;
import com.evolveum.midpoint.client.api.query.FilterExit;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

public class FilterBuilder<O extends ObjectType> implements FilterEntryOrEmpty<O>, AtomicFilterExit<O> {

	private NAryLogicalOperatorFilterClauseType currentFilter;
	private FilterLogicalSymbol lastLogicalSymbol;

	private RestJaxbService service;
	private Class<O> type;

	private PagingType paging;

	public FilterBuilder(RestJaxbService service, Class<O> type) {
		this.service = service;
		this.type = type;
		this.currentFilter = new NAryLogicalOperatorFilterClauseType();
		lastLogicalSymbol = null;

	}

	private FilterBuilder(RestJaxbService service, Class<O> type, NAryLogicalOperatorFilterClauseType currentFilter, FilterLogicalSymbol logicalSymbol, PagingType paging) {
		this.service = service;
		this.type = type;
		this.currentFilter = currentFilter;
		this.lastLogicalSymbol = logicalSymbol;
		this.paging = paging;
	}


	public FilterBuilder<O> addSubfilter(Element subfilter, boolean negated) {
        if (!currentFilter.getFilterClause().isEmpty() && lastLogicalSymbol == null) {
            throw new IllegalStateException("lastLogicalSymbol is empty but there is already some filter present: " + currentFilter);
        }
            NAryLogicalOperatorFilterClauseType newFilter = appendAtomicFilter(subfilter, negated, lastLogicalSymbol);
            return new FilterBuilder<>(service, type, newFilter, null, paging);
    }

	private NAryLogicalOperatorFilterClauseType appendAtomicFilter(Element subfilter, boolean negated, FilterLogicalSymbol lastLogicalSymbol) {
		 DomSerializer dom = service.getDomSerializer();
		if (negated) {
			subfilter = dom.createNotFilter(subfilter);
//	            subfilter = null;// TODO: dom.createNotFilter()
	        }

		 NAryLogicalOperatorFilterClauseType updatedFilter = new NAryLogicalOperatorFilterClauseType();
		 updatedFilter.getFilterClause().addAll(currentFilter.getFilterClause());
		 updatedFilter.setMatching(currentFilter.getMatching());



	        if (lastLogicalSymbol == null || lastLogicalSymbol == FilterLogicalSymbol.OR) {
	        	updatedFilter.getFilterClause().add(dom.createAndFilter(subfilter));
	        } else if (lastLogicalSymbol == FilterLogicalSymbol.AND) {
	            Element andFilter = (getLastCondition(updatedFilter));
	            dom.addCondition(andFilter, subfilter);
	        } else {
	            throw new IllegalStateException("Unknown logical symbol: " + lastLogicalSymbol);
	        }
	        return updatedFilter;


	}

	public Element getLastCondition(NAryLogicalOperatorFilterClauseType updatedFilter) {
		List<Element> conditions = updatedFilter.getFilterClause();
		if (conditions.isEmpty()) {
			return null;
		} else {
			return conditions.get(conditions.size()-1);
		}
	}


	@Override
	public SearchService<O> build() {
		QueryType queryType = new QueryType();
		queryType.setFilter(buildFilter());

		if (paging != null) {
			queryType.setPaging(paging);
		}
		return new RestJaxbSearchService<>(service, type, queryType);

	}

    @Override
    public SearchService<O> build(QueryType query) {
        return new RestJaxbSearchService<>(service, type, query);
    }


	@Override
	public ConditionEntry<O> item(ItemPathType itemPath) {
		return RestJaxbQueryBuilder.create(service, type, this, itemPath); //.item(itemPath);
	}


	@Override
	public ConditionEntry<O> item(QName... qnames) {
		String path = "";
		for (QName name : qnames) {
			path += name + "/";
		}
		ItemPathType itemPath = new ItemPathType();
		itemPath.setValue(StringUtils.removeEnd(path, "/"));
		return RestJaxbQueryBuilder.create(service, type, this, itemPath); //.item(qnames);
	}

	//TODO: Maybe we can re-structure interfaces to exclude some of the duplicated methods like build and paging
//	@Override
//	public PagingRuleBuilder<O> paging()
//	{
//		return null;
//	}

	@Override
	public AtomicFilterExit<O> isDirectChildOf(String oid) {
		ObjectReferenceType refValue = new ObjectReferenceType();
		refValue.setOid(oid);
		return isDirectChildOf(refValue);
	}

	@Override
	public AtomicFilterExit<O> isDirectChildOf(ObjectReferenceType refValue) {
		RestJaxbQueryBuilder<O> builder = RestJaxbQueryBuilder.create(service, type, this, null);
		Element orgFilter = service.getDomSerializer().createOrgFilterRef(refValue, OrgFilterScopeType.ONE_LEVEL);
		return new RestJaxbQueryBuilder<>(builder, orgFilter, this);
	}

	@Override
	public AtomicFilterExit<O> isChildOf(String oid) {
		ObjectReferenceType refValue = new ObjectReferenceType();
		refValue.setOid(oid);
		return isChildOf(refValue);
	}

	@Override
	public AtomicFilterExit<O> isChildOf(ObjectReferenceType refValue) {
		RestJaxbQueryBuilder<O> builder = RestJaxbQueryBuilder.create(service, type, this, null);
		Element orgFilter = service.getDomSerializer().createOrgFilterRef(refValue, OrgFilterScopeType.SUBTREE);
		return new RestJaxbQueryBuilder<>(builder, orgFilter, this);
	}

	@Override
	public AtomicFilterExit<O> isRoot() {
		RestJaxbQueryBuilder<O> builder = RestJaxbQueryBuilder.create(service, type, this, null);
		Element orgFilter = service.getDomSerializer().createOrgFilterRoot(true);
		return new RestJaxbQueryBuilder<>(builder, orgFilter, this);
	}

	@Override
	public FilterEntry<O> and() {
		return setLastLogicalSymbol(FilterLogicalSymbol.AND);
	}

	@Override
	public FilterEntry<O> or() {
		return setLastLogicalSymbol(FilterLogicalSymbol.OR);
	}

	private FilterBuilder<O> setLastLogicalSymbol(FilterLogicalSymbol newLogicalSymbol) {
		if (this.lastLogicalSymbol != null) {
            throw new IllegalStateException("Two logical symbols in a sequence");
        }
        return new FilterBuilder<O>(service, type, currentFilter, newLogicalSymbol, paging);
	}


//	@Override
//	public QueryBuilder<O> finishQuery() {
//		QueryType queryType = new QueryType();
//		queryType.setFilter(buildFilter());
//		return new RestJaxbQueryBuilder<>(service, type, queryType);
//	}

	public SearchFilterType buildFilter() {
		SearchFilterType filter = new SearchFilterType();
		if (currentFilter.getFilterClause().size() == 1) {
			Element firstFilter = currentFilter.getFilterClause().iterator().next();
			if (firstFilter.getTagName().equals("and")) {
				if (firstFilter.getChildNodes() != null && firstFilter.getChildNodes().getLength() == 1) {
					filter.setFilterClause((Element) firstFilter.getFirstChild());
				} else {
					filter.setFilterClause(firstFilter);
				}
			} else {
				filter.setFilterClause(firstFilter);
			}
		} else {
			Element orFilter = service.getDomSerializer().createOrFilter(currentFilter.getFilterClause());
			filter.setFilterClause(orFilter);
		}
		return filter;
	}

@Override
public AtomicFilterEntry<O> not() {
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

	String pathValue = "";
	for (QName qname : names) {
		pathValue += qname.getLocalPart() + "/";
	}
	ItemPathType path = new ItemPathType();

	path.setValue(StringUtils.removeEnd(pathValue, "/"));;
	return addOrdering(path, OrderDirectionType.ASCENDING);

}

@Override
public FilterExit<O> asc(ItemPathType path) {
	return addOrdering(path, OrderDirectionType.ASCENDING);
}

@Override
public FilterExit<O> desc(QName... names) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public FilterExit<O> desc(ItemPathType path) {
	return addOrdering(path, OrderDirectionType.DESCENDING);
}

@Override
public FilterExit<O> offset(Integer n) {
	return setOffset(n);
}

@Override
public FilterExit<O> maxSize(Integer n) {
	return setMaxSize(n);
}

private FilterBuilder<O> addOrdering(ItemPathType orderBy, OrderDirectionType direction) {
	paging = getPaging();
	paging.setOrderDirection(direction);
	paging.setOrderBy(orderBy);

	return new FilterBuilder<>(service, type, currentFilter, null, paging);
}

private FilterBuilder<O> setOffset(Integer n) {
	paging = getPaging();
	paging.setOffset(n);

	return new FilterBuilder<>(service, type, currentFilter, null, paging);
}

private FilterBuilder<O> setMaxSize(Integer n) {
	paging = getPaging();
	paging.setMaxSize(n);

	return new FilterBuilder<>(service, type, currentFilter, null, paging);
}

private PagingType getPaging() {
	if (paging == null) {
		paging = new PagingType();
	}

	return paging;
}


}
