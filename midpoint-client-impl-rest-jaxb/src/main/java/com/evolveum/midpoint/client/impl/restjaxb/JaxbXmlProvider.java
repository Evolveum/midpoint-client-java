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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.apache.cxf.jaxrs.provider.AbstractJAXBProvider;

/**
 *
 * @author katkav
 *
 */
@Produces({"application/xml"})
@Consumes({"application/xml"})
@Provider
public class JaxbXmlProvider<T> extends AbstractJAXBProvider<T>{

	private JAXBContext jaxbContext;

	public JaxbXmlProvider(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T readFrom(Class<T> clazz, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4,
			InputStream inputStream) throws IOException, WebApplicationException {

		if (inputStream == null || inputStream.available() == 0) {
			return null;
		}
			try {

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Object object = unmarshaller.unmarshal(inputStream);
			if (object instanceof JAXBElement) {
				return (T) ((JAXBElement<?>) object).getValue();
			}
			return (T) object;
			} catch (JAXBException e) {
				throw new IOException(e);
			}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeTo(T jaxbElement, Class<?> clazz, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream outputStream) throws IOException, WebApplicationException {
		try {


		Marshaller marshaller = jaxbContext.createMarshaller();
		@SuppressWarnings("rawtypes")
		JAXBElement<T> element = new JAXBElement(Types.findType(clazz).getElementName(), clazz, jaxbElement);
		marshaller.marshal(element, outputStream);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}




}
