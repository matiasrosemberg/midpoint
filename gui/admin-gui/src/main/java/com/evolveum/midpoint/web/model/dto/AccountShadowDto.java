/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 * Portions Copyrighted 2010 Forgerock
 */

package com.evolveum.midpoint.web.model.dto;

import com.evolveum.midpoint.xml.ns._public.common.common_1.AccountShadowType;

/**
 * 
 * @author semancik
 */
public class AccountShadowDto extends ResourceObjectShadowDto<AccountShadowType> {

	private static final long serialVersionUID = -7908853369766852050L;

	public AccountShadowDto(AccountShadowType object) {
		super(object);
	}

	public AccountShadowDto() {
	}
}
