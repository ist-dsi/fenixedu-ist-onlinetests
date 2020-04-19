/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on 3/Fev/2004
 *
 */
package org.fenixedu.academic.domain.onlineTests;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.bennu.core.domain.Bennu;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * @author Susana Fernandes
 * 
 */
public class TestScope extends TestScope_Base {

    public TestScope() {
        setRootDomainObject(Bennu.getInstance());
    }

    public TestScope(final ExecutionCourse executionCourse) {
        this();
        setExecutionCourse(executionCourse);
    }

    public static List<DistributedTest> readDistributedTestsByTestScope(final ExecutionCourse executionCourse) {
        final TestScope testScope = executionCourse.getTestScope();
        return testScope == null ? Collections.emptyList() : testScope.getSortedDistributedTestsSet();
    }

    public List<DistributedTest> getSortedDistributedTestsSet() {
        return getDistributedTestsSet().stream()
                .sorted(DistributedTest.COMPARATOR_BY_DATE)
                .collect(Collectors.toList());
    }

    public List<Test> getSortedTestsSet() {
        return getTestsSet().stream()
                .sorted((t1, t2) -> t1.getCreationDateDateTime().compareTo(t2.getCreationDateDateTime()))
                .collect(Collectors.toList());
    }
}
