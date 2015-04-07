package com.dnastack.bob.rest.resource.app;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.dnastack.bob.rest.util.ExceptionHandler;
import com.dnastack.bob.rest.util.LoggingFilter;
import com.dnastack.bob.rest.resource.AllleleResource;
import com.dnastack.bob.rest.resource.BeaconResource;
import com.dnastack.bob.rest.resource.BeaconResponseResource;
import com.dnastack.bob.rest.resource.ChromosomeResource;
import com.dnastack.bob.rest.resource.ReferenceResource;
import com.dnastack.bob.rest.resource.RestEndPointResource;
import com.dnastack.bob.rest.util.CORSFilter;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class SwaggerJaxrsResourceConfiguration extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> resources = new HashSet<>();
		resources
				.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
		resources
				.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
		resources
				.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
		resources
				.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);
		addRestResourceClasses(resources);
		return resources;
	}

	private void addRestResourceClasses(Set<Class<?>> resources) {
		resources.add(RestEndPointResource.class);
		resources.add(BeaconResource.class);
		resources.add(BeaconResponseResource.class);
		resources.add(ReferenceResource.class);
		resources.add(ChromosomeResource.class);
		resources.add(AllleleResource.class);
		resources.add(LoggingFilter.class);
		resources.add(CORSFilter.class);
		resources.add(ExceptionHandler.class);
	}
}