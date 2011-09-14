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
 * Portions Copyrighted 2011 []
 * Portions Copyrighted 2011 Igor Farinic
 * Portions Copyrighted 2011 Peter Prochazka
 */
package com.evolveum.midpoint.util.aspect;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class MidpointAspect {

	//This logger provide profiling informations
	private static final org.slf4j.Logger LOGGER_PROFILING = org.slf4j.LoggerFactory.getLogger("PROFILING");

	// FIXME: try to switch to spring injection. Note: infra components
	// shouldn't depend on spring
	// Formatters are statically initialized from class common's DebugUtil
	private static List<ObjectFormatter> formatters = new ArrayList<ObjectFormatter>();

	/**
	 * Register new formatter
	 * @param formatter
	 */
	public static void registerFormatter(ObjectFormatter formatter) {
		formatters.add(formatter);
	}

	@Around("entriesIntoRepository()")
	public Object processRepositoryNdc(ProceedingJoinPoint pjp) throws Throwable {
		return markSubsystem(pjp, "REPOSITORY");
	}

	@Around("entriesIntoTaskManager()")
	public Object processTaskManagerNdc(ProceedingJoinPoint pjp) throws Throwable {
		return markSubsystem(pjp, "TASKMANAGER");
	}

	@Around("entriesIntoProvisioning()")
	public Object processProvisioningNdc(ProceedingJoinPoint pjp) throws Throwable {
		return markSubsystem(pjp, "PROVISIONING");
	}

	@Around("entriesIntoResourceObjectChangeListener()")
	public Object processResourceObjectChangeListenerNdc(ProceedingJoinPoint pjp) throws Throwable {
		return markSubsystem(pjp, "RESOURCEOBJECTCHANGELISTENER");
	}

	@Around("entriesIntoModel()")
	public Object processModelNdc(ProceedingJoinPoint pjp) throws Throwable {
		return markSubsystem(pjp, "MODEL");
	}

	@Around("entriesIntoWeb()")
	public Object processWebNdc(ProceedingJoinPoint pjp) throws Throwable {
		return markSubsystem(pjp, "WEB");
	}

	private Object markSubsystem(ProceedingJoinPoint pjp, String subsystem) throws Throwable {
		Object retValue = null;
		String prev = null;
		//Profiling start
		long startTime = System.nanoTime();
		
		try {
			//Marking MDC->Subsystem with current one subsystem and mark previous
			prev = (String) MDC.get("subsystem");
			MDC.put("subsystem", subsystem);
			
			//is profiling info is needed
			if (LOGGER_PROFILING.isInfoEnabled()) {
				LOGGER_PROFILING.info("#### Entry: {}->{}", getClassName(pjp), pjp.getSignature().getName());
				//If debug enable get entry parameters and log them
				if (LOGGER_PROFILING.isDebugEnabled()) {
					final Object[] args = pjp.getArgs();
					//				final String[] names = ((CodeSignature) pjp.getSignature()).getParameterNames();
					//				@SuppressWarnings("unchecked")
					//				final Class<CodeSignature>[] types = ((CodeSignature) pjp.getSignature()).getParameterTypes();
					final StringBuffer sb = new StringBuffer();
					sb.append("###### args: ");
					sb.append("(");
					for (int i = 0; i < args.length; i++) {
						sb.append(formatVal(args[i]));
						if (args.length == i + 1) {
							sb.append(")");
						} else {
							sb.append(", ");
						}
					}
					LOGGER_PROFILING.debug(sb.toString());
				}
			}

			//Process original call
			retValue = pjp.proceed();

			//Return original response
			return retValue;

		} finally {
			//Restore previously marked subsystem executed before return
			
			if (LOGGER_PROFILING.isInfoEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("##### Exit: ");
				sb.append(getClassName(pjp));
				sb.append("->");
				sb.append(pjp.getSignature().getName());
				sb.append(" etime: ");
				//Mark end of processing
				long elapsed = System.nanoTime() - startTime;
				sb.append((long) (elapsed / 1000000));
				sb.append('.');
				long mikros = (long) (elapsed / 1000) % 1000;
				if (mikros < 100) {
					sb.append('0');
				}
				if (mikros < 10) {
					sb.append('0');
				}
				sb.append(mikros);
				sb.append(" ms");
				LOGGER_PROFILING.info(sb.toString());
				if (LOGGER_PROFILING.isDebugEnabled()) {
					LOGGER_PROFILING.debug("###### retval: {}", formatVal(retValue));
				}
			}
			//Restore MDC
			if (prev == null) {
				MDC.remove("subsystem");
			} else {
				MDC.put("subsystem", prev);
			}
		}
	}

	@Pointcut("execution(* com.evolveum.midpoint.repo.api.RepositoryService.*(..))")
	public void entriesIntoRepository() {
	}

	@Pointcut("execution(* com.evolveum.midpoint.task.api.TaskManager.*(..))")
	public void entriesIntoTaskManager() {
	}

	@Pointcut("execution(* com.evolveum.midpoint.provisioning.api.ProvisioningService.*(..))")
	public void entriesIntoProvisioning() {
	}

	@Pointcut("execution(* com.evolveum.midpoint.provisioning.api.ResourceObjectChangeListener.*(..))")
	public void entriesIntoResourceObjectChangeListener() {
	}

	@Pointcut("execution(* com.evolveum.midpoint.model.api.ModelService.*(..))")
	public void entriesIntoModel() {
	}

	@Pointcut("execution(* com.evolveum.midpoint.web.controller.*(..))")
	public void entriesIntoWeb() {
	}

	/**
	 * Get joinpoint class name if available
	 * @param pjp
	 * @return
	 */
	private String getClassName(ProceedingJoinPoint pjp) {
		if (pjp.getThis() != null) {
			return pjp.getThis().getClass().getName();
		}
		return null;
	}

	/**
	 * Debug output formater
	 * @param value
	 * @return
	 */

	private String formatVal(Object value) {
		if (value == null) {
			return ("null");
		} else {
			String out = null;
			for (ObjectFormatter formatter : formatters) {
				out = formatter.format(value);
				if (out != null) {
					break;
				}
			}
			if (out == null) {
				return (value.toString());
			} else {
				return out;
			}
		}
	}
}
