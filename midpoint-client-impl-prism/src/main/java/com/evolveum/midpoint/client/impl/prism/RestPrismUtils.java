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

import java.util.List;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

/**
 * @author Z14tk0
 */
public final class RestPrismUtils {

    private RestPrismUtils() {
        throw new UnsupportedOperationException("Class cannot be instantiated.");
    }

    public static ObjectModificationType buildModifyObject(List<ItemDeltaType> itemDeltas) {
        ObjectModificationType objectModificationType = new ObjectModificationType();
        itemDeltas.forEach(itemDelta -> objectModificationType.getItemDelta().add(itemDelta));

        return objectModificationType;
    }

    public static ItemDeltaType buildItemDelta(ModificationTypeType modificationType, String path, Object value) {
        //Create ItemDelta
        ItemDeltaType itemDeltaType = new ItemDeltaType();
        itemDeltaType.setModificationType(modificationType);

        //Set Path
        ItemPathType itemPathType = new ItemPathType(ItemPath.fromString(path));
        itemDeltaType.setPath(itemPathType);

        if (value != null) {
            itemDeltaType.getValue().add(RawType.fromPropertyRealValue(value, null));   //TODO recheck if this works in all cases?
        }

        return itemDeltaType;
    }
}
