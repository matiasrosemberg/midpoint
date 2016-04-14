/*
 * Copyright (c) 2010-2016 Evolveum
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
package com.evolveum.midpoint.web.page.admin.workflow;

import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.web.component.AbstractSummaryPanel;
import com.evolveum.midpoint.web.component.DateLabelComponent;
import com.evolveum.midpoint.web.component.util.SummaryTagSimple;
import com.evolveum.midpoint.web.page.admin.workflow.dto.WorkItemDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemType;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import javax.xml.namespace.QName;

/**
 * @author mederly
 *
 */
public class WorkItemSummaryPanel extends AbstractSummaryPanel<WorkItemType> {
	private static final long serialVersionUID = -5077637168906420769L;

	private static final String ID_ASSIGNED_TAG = "assignedTag";

	private final IModel<WorkItemDto> dtoModel;

	public WorkItemSummaryPanel(String id, IModel<WorkItemType> model, IModel<WorkItemDto> dtoModel) {
		super(id, model);
		this.dtoModel = dtoModel;

		initLayoutCommon();

		SummaryTagSimple<WorkItemType> isAssignedTag = new SummaryTagSimple<WorkItemType>(ID_ASSIGNED_TAG, model) {
			@Override
			protected void initialize(WorkItemType workItem) {
				if (workItem.getAssigneeRef() != null) {
					setIconCssClass("fa fa-fw fa-lg fa-lock");
					setLabel(getString("WorkItemSummaryPanel.assigned"));
				} else {
					setIconCssClass("fa fa-fw fa-lg fa-unlock");
					setLabel(getString("WorkItemSummaryPanel.notAssigned"));
				}
			}
		};
		addTag(isAssignedTag);
	}

	@Override
	protected String getIconCssClass() {
		return GuiStyleConstants.CLASS_OBJECT_WORK_ITEM_ICON;
	}

	@Override
	protected String getIconBoxAdditionalCssClass() {		// TODO
		return "summary-panel-task"; // TODO
	}

	@Override
	protected String getBoxAdditionalCssClass() {			// TODO
		return "summary-panel-task"; // TODO
	}

	@Override
	protected boolean isIdentifierVisible() {
		return false;
	}

	@Override
	protected String getTagBoxCssClass() {
		return "summary-tag-box";
	}

	@Override
	protected IModel<String> getTitleModel() {
		return new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return getString("TaskSummaryPanel.requestedBy",
						WebComponentUtil.getName(dtoModel.getObject().getRequester()));
			}
		};
	}

	@Override
	protected IModel<String> getTitle2Model() {
		return new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				WorkItemType workItem = getModelObject();
				return getString("TaskSummaryPanel.requestedOn",
						WebComponentUtil.getLocalizedDate(workItem.getProcessStartedTimestamp(), DateLabelComponent.MEDIUM_MEDIUM_STYLE));
			}
		};
	}

//	@Override
//	protected IModel<String> getTitle3Model() {
//		return new AbstractReadOnlyModel<String>() {
//			@Override
//			public String getObject() {
//				WorkItemType workItem = getModelObject();
//				return getString("WorkItemSummaryPanel.createdOn",
//						workItem.getWorkItemCreatedTimestamp());		// todo formatting
//			}
//		};
//	}
}
