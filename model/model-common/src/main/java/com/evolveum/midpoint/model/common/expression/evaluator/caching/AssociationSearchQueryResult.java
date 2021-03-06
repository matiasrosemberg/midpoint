/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.model.common.expression.evaluator.caching;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.util.ShadowUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowAssociationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowKindType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * @author Pavol Mederly
 */
public class AssociationSearchQueryResult extends QueryResult<PrismContainerValue<ShadowAssociationType>> {

    private String resourceOid;
    private ShadowKindType kind;

    public AssociationSearchQueryResult(List<PrismContainerValue<ShadowAssociationType>> resultingList, List<PrismObject<ShadowType>> rawResultsList) {
        super(resultingList);

        Validate.isTrue(rawResultsList != null && !rawResultsList.isEmpty());
        ShadowType shadow = rawResultsList.get(0).asObjectable();

        resourceOid = ShadowUtil.getResourceOid(shadow);
        kind = shadow.getKind();
    }

    public String getResourceOid() {
        return resourceOid;
    }

    public ShadowKindType getKind() {
        return kind;
    }
}
