package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.category.resource.CategoryResource;
import org.innovateuk.ifs.category.resource.CategoryType;
import org.innovateuk.ifs.category.service.CategoryRestServiceImpl;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.categoryResourceListType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests to check the CategoryRestService's interaction with the RestTemplate and the processing of its results
 */
public class CategoryRestServiceMocksTest extends BaseRestServiceUnitTest<CategoryRestServiceImpl> {

    private static final String categoryRestURL = "/category";

    @Override
    protected CategoryRestServiceImpl registerRestServiceUnderTest() {
        return new CategoryRestServiceImpl();
    }


    @Test
    public void test_findByType() {

        String expectedUrl = categoryRestURL + "/findByType/" + CategoryType.INNOVATION_AREA.getName();
        List<CategoryResource> returnedCategoryResources = Arrays.asList(1,2,3).stream().map(i -> new CategoryResource()).collect(Collectors.toList());

        setupGetWithRestResultExpectations(expectedUrl, categoryResourceListType(), returnedCategoryResources);

        // now run the method under test
        List<CategoryResource> categoryResources = service.getByType(CategoryType.INNOVATION_AREA).getSuccessObjectOrThrowException();
        assertNotNull(categoryResources);
        assertEquals(returnedCategoryResources, categoryResources);
    }

    @Test
    public void test_findByParent() {

        String expectedUrl = categoryRestURL + "/findByParent/1";
        List<CategoryResource> returnedCategoryResources = Arrays.asList(1,2,3).stream().map(i -> new CategoryResource()).collect(Collectors.toList());
        setupGetWithRestResultExpectations(expectedUrl, categoryResourceListType(), returnedCategoryResources);

        // now run the method under test
        List<CategoryResource> categoryResources = service.getByParent(1L).getSuccessObjectOrThrowException();
        assertNotNull(categoryResources);
        assertEquals(returnedCategoryResources, categoryResources);
    }
}