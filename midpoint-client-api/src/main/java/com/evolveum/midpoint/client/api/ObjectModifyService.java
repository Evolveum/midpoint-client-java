package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * @author jakmor
 */
public interface ObjectModifyService <O extends ObjectType> extends Post<ObjectReference<O>>{
}
